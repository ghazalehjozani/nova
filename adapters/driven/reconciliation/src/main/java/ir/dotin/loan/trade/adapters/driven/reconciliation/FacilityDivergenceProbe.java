package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.util.Optional;

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
            return Divergence.unknown("nova-missing");
        }
        FacilityStatus novaStatus = novaRow.get().status();

        Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId);
        if (fcbResult.isFailure()) {
            log.debug("FCB recon-state read failed for facility {} — UNKNOWN", facilityId);
            return Divergence.unknown("fcb-unreachable");
        }
        ReconLoanFileState fcb = fcbResult.unwrap();
        if (!fcb.reachable()) {
            return Divergence.unknown("fcb-unreachable");
        }

        return classify(novaStatus, fcb);
    }

    private static Divergence classify(FacilityStatus novaStatus, ReconLoanFileState fcb) {
        String fcbFileStatus = fcb.fileStatus();

        // INV-1: terminal-dominant. Nova terminal + FCB reflects it (absent or revoked) → aligned.
        if (FacilityReconMapping.isTerminal(novaStatus)) {
            boolean fcbReflectsTerminal = !fcb.exists() || FacilityReconMapping.isFcbRevoked(fcbFileStatus);
            if (fcbReflectsTerminal) {
                return Divergence.aligned().withDetail(FacilityReconMapping.observedDetail(novaStatus, fcbFileStatus));
            }
            // Nova terminal but FCB still shows an active file — a divergence an operator must judge (revoke path).
            return Divergence.lagging(Direction.SOURCE_AHEAD, "nova-terminal-fcb-active")
                    .withDetail(FacilityReconMapping.observedDetail(novaStatus, fcbFileStatus));
        }

        // Non-terminal Nova: if FCB has no file at all, the forward create event never landed → ORPHAN.
        if (!fcb.exists()) {
            return Divergence.orphan(Direction.SOURCE_AHEAD, "fcb-absent")
                    .withDetail(FacilityReconMapping.observedDetail(novaStatus, null));
        }

        // FCB has a file: compare expected forward status. If FCB is behind Nova's expectation → LAGGING.
        if (FacilityReconMapping.fcbBehind(novaStatus, fcbFileStatus)) {
            return Divergence.lagging(Direction.SOURCE_AHEAD, "fcb-behind")
                    .withDetail(FacilityReconMapping.observedDetail(novaStatus, fcbFileStatus));
        }

        return Divergence.aligned().withDetail(FacilityReconMapping.observedDetail(novaStatus, fcbFileStatus));
    }
}
