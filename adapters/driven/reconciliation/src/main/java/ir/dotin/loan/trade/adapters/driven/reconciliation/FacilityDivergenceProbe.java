package ir.dotin.loan.trade.adapters.driven.reconciliation;

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
import ir.dotin.platform.pangaea.reconciliation.api.spi.DivergenceProbe;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbReconStatePort;
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

    private final FacilityReconReadPort readPort;
    private final FcbReconStatePort fcbReconStatePort;

    @Override
    public ReconciliationType type() {
        return FacilityReconciliationSource.TYPE;
    }

    @Override
    public Divergence probe(OpaqueKey key) {
        String facilityId = key.value();

        Optional<FacilityReconRow> novaRow = readPort.findById(facilityId);
        if (novaRow.isEmpty()) {
            // Abnormal: the source aggregate vanished (Nova does not hard-delete facilities in normal ops). Surface
            // it (INV-13 will age the resulting UNKNOWN row to NEEDS_OPERATOR). reasonCode + detail both carry "why".
            log.warn("Recon probe {}: Nova facility row absent — UNKNOWN(nova-missing)", facilityId);
            return Divergence.unknown("nova-missing").withDetail(Map.of("novaStatus", "<absent>"));
        }
        FacilityStatus novaStatus = novaRow.get().status();
        String applicationNumber = novaRow.get().applicationNumber();

        Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId);
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
        logVerdict(facilityId, novaStatus, fcb, verdict);
        return verdict;
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
