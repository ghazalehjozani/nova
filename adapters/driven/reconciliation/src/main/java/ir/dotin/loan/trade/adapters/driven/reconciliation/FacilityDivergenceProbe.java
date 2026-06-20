package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.reconciliation.api.model.Direction;
import ir.dotin.platform.pangaea.reconciliation.api.model.Divergence;
import ir.dotin.platform.pangaea.reconciliation.api.model.OpaqueKey;
import ir.dotin.platform.pangaea.reconciliation.api.model.ReconciliationType;
import ir.dotin.platform.pangaea.reconciliation.api.model.Verdict;
import ir.dotin.platform.pangaea.reconciliation.api.spi.DivergenceProbe;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbReconStatePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconGuarantor;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;

import lombok.RequiredArgsConstructor;

/**
 * Divergence probe for the {@code facility-state} reconciliation type. Reads the current authoritative Nova facility
 * status and FCB's current loan-file state and returns a {@link Divergence} verdict.
 *
 * <p>Safety invariants:
 *
 * <ul>
 *   <li>INV-2: a Result failure or {@code !reachable} FCB answer is always {@code UNKNOWN} (never {@code ORPHAN}) — we
 *       never declare FCB absent on an inconclusive read.
 *   <li>INV-1: terminal-dominant — a Nova terminal status that FCB reflects (absent or revoked) is {@code ALIGNED}.
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class FacilityDivergenceProbe implements DivergenceProbe {

    private static final Logger log = LoggerFactory.getLogger(FacilityDivergenceProbe.class);

    /** Reason code carried on a guarantor-drift {@link Divergence} (a LAGGING-style divergence). */
    static final String GUARANTOR_DRIFT_REASON = "guarantor-drift";

    private final FacilityReconReadPort readPort;
    private final FcbReconStatePort fcbReconStatePort;
    private final ReconciliationSourceProperties properties;

    @Override
    public ReconciliationType type() {
        return FacilityReconciliationSource.TYPE;
    }

    @Override
    public Divergence probe(OpaqueKey key) {
        String facilityId = key.value();

        // Only pay for the (extra-query) guarantor projection when the guarantor-drift feature is enabled — flag off =>
        // no findGuarantorsById query at all.
        Optional<FacilityReconRow> novaRow = readPort.findById(facilityId, properties.isGuarantorDriftEnabled());
        if (novaRow.isEmpty()) {
            // Abnormal: the source aggregate vanished (Nova does not hard-delete facilities in normal ops). Surface
            // it (INV-13 will age the resulting UNKNOWN row to NEEDS_OPERATOR). reasonCode + detail both carry "why".
            log.warn("Recon probe {}: Nova facility row absent — UNKNOWN(nova-missing)", facilityId);
            return Divergence.unknown("nova-missing").withDetail(Map.of("novaStatus", "<absent>"));
        }
        FacilityStatus novaStatus = novaRow.get().status();
        String applicationNumber = novaRow.get().applicationNumber();

        // State-only probe: the divergence VERDICT (ORPHAN/LAGGING/ALIGNED) needs only exists/fileStatus, not the
        // per-event peer signals (those are gathered at classify/converge time). Pass no uids so FCB skips the signal
        // gather (LN-59513).
        Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId, null);
        if (fcbResult.isFailure()) {
            log.info(
                    "Recon probe {}: FCB recon-state read failed (novaStatus={}) — UNKNOWN(fcb-unreachable): {}",
                    facilityId,
                    novaStatus,
                    fcbResult.err().map(Object::toString).orElse("<no-cause>"));
            return Divergence.unknown("fcb-unreachable").withDetail(unreachableDetail(novaStatus, applicationNumber));
        }
        ReconLoanFileState fcb = fcbResult.unwrap();
        if (!fcb.reachable()) {
            log.info(
                    "Recon probe {}: FCB reported unreachable (novaStatus={}) — UNKNOWN(fcb-unreachable)",
                    facilityId,
                    novaStatus);
            return Divergence.unknown("fcb-unreachable").withDetail(unreachableDetail(novaStatus, applicationNumber));
        }

        Divergence verdict = classify(novaStatus, fcb, applicationNumber);

        // Guarantor-drift overlay (dark-launch, flag-gated): only meaningful when the status axis is otherwise ALIGNED
        // (a non-terminal facility whose FCB file is at the expected rank). A status divergence already wins — never
        // mask a LAGGING/ORPHAN status verdict with a guarantor verdict.
        if (verdict.verdict() == Verdict.ALIGNED
                && properties.isGuarantorDriftEnabled()
                && !FacilityReconMapping.isTerminal(novaStatus)
                && fcb.exists()) {
            Divergence driftVerdict =
                    detectGuarantorDrift(novaRow.get().guarantors(), fcb, novaStatus, applicationNumber);
            if (driftVerdict != null) {
                logVerdict(facilityId, novaStatus, fcb, driftVerdict);
                return driftVerdict;
            }
        }

        logVerdict(facilityId, novaStatus, fcb, verdict);
        return verdict;
    }

    /**
     * Compares Nova's current guarantor set against FCB's by {@code Map<customerNumber, percentage>}: the
     * customer-number keysets must match AND each percentage must be equal by {@link BigDecimal#compareTo} (ignoring
     * scale — FCB sends "100.0000", Nova "100"). A mismatch yields a {@code guarantor-drift} LAGGING divergence (Nova
     * is the source of truth for guarantors, so {@code SOURCE_AHEAD}). Returns {@code null} (no drift) when the sets
     * match OR when either side's guarantor data is unusable (FCB returned none, or any percentage is unknown) — a
     * missing side is treated as unknown and never flagged, so the detector can never false-positive.
     */
    private static @Nullable Divergence detectGuarantorDrift(
            List<ReconGuarantor> novaGuarantors,
            ReconLoanFileState fcb,
            FacilityStatus novaStatus,
            @Nullable String applicationNumber) {
        List<ReconGuarantor> fcbGuarantors = fcb.guarantors();
        if (fcbGuarantors.isEmpty()) {
            // FCB returned no guarantor data (or an older FCB build predating the field) → unknown, never flag.
            return null;
        }

        Map<String, BigDecimal> novaByCustomer = indexByCustomer(novaGuarantors);
        Map<String, BigDecimal> fcbByCustomer = indexByCustomer(fcbGuarantors);
        if (novaByCustomer == null || fcbByCustomer == null) {
            // A null/blank customer number or an unknown percentage on either side → unknown, never flag.
            return null;
        }

        if (!novaByCustomer.keySet().equals(fcbByCustomer.keySet())) {
            return guarantorDriftDivergence(novaStatus, fcb, applicationNumber);
        }
        for (Map.Entry<String, BigDecimal> entry : novaByCustomer.entrySet()) {
            BigDecimal fcbPercent = fcbByCustomer.get(entry.getKey());
            if (fcbPercent == null || entry.getValue().compareTo(fcbPercent) != 0) {
                return guarantorDriftDivergence(novaStatus, fcb, applicationNumber);
            }
        }
        return null;
    }

    /**
     * Indexes a guarantor list by customer number → percentage, or returns {@code null} when any element has a
     * null/blank customer number or an unknown ({@code null}) percentage (so the caller treats the side as unknown and
     * never flags drift).
     */
    private static @Nullable Map<String, BigDecimal> indexByCustomer(List<ReconGuarantor> guarantors) {
        Map<String, BigDecimal> byCustomer = new HashMap<>();
        for (ReconGuarantor guarantor : guarantors) {
            String customerNumber = guarantor.customerNumber();
            BigDecimal percentage = guarantor.guaranteePercentage();
            if (customerNumber == null || customerNumber.isBlank() || percentage == null) {
                return null;
            }
            byCustomer.put(customerNumber, percentage);
        }
        return byCustomer;
    }

    private static Divergence guarantorDriftDivergence(
            FacilityStatus novaStatus, ReconLoanFileState fcb, @Nullable String applicationNumber) {
        return Divergence.lagging(Direction.SOURCE_AHEAD, GUARANTOR_DRIFT_REASON)
                .withDetail(FacilityReconMapping.observedDetail(novaStatus, fcb.fileStatus(), applicationNumber));
    }

    private static Map<String, String> unreachableDetail(
            FacilityStatus novaStatus, @Nullable String applicationNumber) {
        return Map.of(
                "novaStatus",
                novaStatus.name(),
                "fcbProbe",
                "unreachable",
                "applicationNumber",
                applicationNumber == null || applicationNumber.isBlank() ? "<absent>" : applicationNumber);
    }

    /**
     * Diverged/aligned verdicts at INFO/DEBUG so the reason is visible from logs (UNKNOWN reasons are logged above).
     */
    private static void logVerdict(String facilityId, FacilityStatus novaStatus, ReconLoanFileState fcb, Divergence v) {
        switch (v.verdict()) {
            case ORPHAN, LAGGING ->
                log.info(
                        "Recon probe {}: {} ({}) — novaStatus={}, fcbExists={}, fcbFileStatus={}",
                        facilityId,
                        v.verdict(),
                        v.reasonCode(),
                        novaStatus,
                        fcb.exists(),
                        fcb.fileStatus());
            case ALIGNED ->
                log.debug(
                        "Recon probe {}: ALIGNED — novaStatus={}, fcbFileStatus={}",
                        facilityId,
                        novaStatus,
                        fcb.fileStatus());
            case UNKNOWN -> {
                // unreachable/missing UNKNOWN is logged before classify(); classify() never returns UNKNOWN.
            }
        }
    }

    private static Divergence classify(
            FacilityStatus novaStatus, ReconLoanFileState fcb, @Nullable String applicationNumber) {
        String fcbFileStatus = fcb.fileStatus();

        // INV-1: terminal-dominant. Nova terminal + FCB reflects it (absent or revoked) → aligned.
        if (FacilityReconMapping.isTerminal(novaStatus)) {
            boolean fcbReflectsTerminal = !fcb.exists() || FacilityReconMapping.isFcbRevoked(fcbFileStatus);
            if (fcbReflectsTerminal) {
                return Divergence.aligned()
                        .withDetail(FacilityReconMapping.observedDetail(novaStatus, fcbFileStatus, applicationNumber));
            }
            // Nova terminal but FCB still shows an active file — a divergence an operator must judge (revoke path).
            return Divergence.lagging(Direction.SOURCE_AHEAD, "nova-terminal-fcb-active")
                    .withDetail(FacilityReconMapping.observedDetail(novaStatus, fcbFileStatus, applicationNumber));
        }

        // Non-terminal Nova: if FCB has no file at all, the forward create event never landed → ORPHAN.
        if (!fcb.exists()) {
            return Divergence.orphan(Direction.SOURCE_AHEAD, "fcb-absent")
                    .withDetail(FacilityReconMapping.observedDetail(novaStatus, null, applicationNumber));
        }

        // FCB has a file: compare expected forward status. If FCB is behind Nova's expectation → LAGGING.
        if (FacilityReconMapping.fcbBehind(novaStatus, fcbFileStatus)) {
            return Divergence.lagging(Direction.SOURCE_AHEAD, "fcb-behind")
                    .withDetail(FacilityReconMapping.observedDetail(novaStatus, fcbFileStatus, applicationNumber));
        }

        return Divergence.aligned()
                .withDetail(FacilityReconMapping.observedDetail(novaStatus, fcbFileStatus, applicationNumber));
    }
}
