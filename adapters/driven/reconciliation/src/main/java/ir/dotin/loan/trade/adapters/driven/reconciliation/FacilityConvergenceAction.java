package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.inbox.api.InboxStatus;
import ir.dotin.platform.pangaea.inbox.api.admin.InboxAdminDetail;
import ir.dotin.platform.pangaea.inbox.api.admin.InboxAdminItem;
import ir.dotin.platform.pangaea.inbox.api.admin.InboxAdminPage;
import ir.dotin.platform.pangaea.inbox.api.admin.InboxAdminPort;
import ir.dotin.platform.pangaea.inbox.api.admin.InboxManualRetryOutcome;
import ir.dotin.platform.pangaea.outbox.api.MessageStatus;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxAdminPort;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxAdminPort.OutboxAdminCriteria;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxAdminPort.OutboxRepublishCommand;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordPage;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordView;
import ir.dotin.platform.pangaea.reconciliation.api.admin.RemediateOutcome;
import ir.dotin.platform.pangaea.reconciliation.api.model.AutonomyTier;
import ir.dotin.platform.pangaea.reconciliation.api.model.Confidence;
import ir.dotin.platform.pangaea.reconciliation.api.model.ConvergeOutcome;
import ir.dotin.platform.pangaea.reconciliation.api.model.Divergence;
import ir.dotin.platform.pangaea.reconciliation.api.model.OpaqueKey;
import ir.dotin.platform.pangaea.reconciliation.api.model.OperatorDossier;
import ir.dotin.platform.pangaea.reconciliation.api.model.ReconciliationType;
import ir.dotin.platform.pangaea.reconciliation.api.model.RemediationKind;
import ir.dotin.platform.pangaea.reconciliation.api.model.RootCause;
import ir.dotin.platform.pangaea.reconciliation.api.model.SafetyTier;
import ir.dotin.platform.pangaea.reconciliation.api.spi.ConvergenceAction;
import ir.dotin.platform.pangaea.workflow.api.admin.WorkflowAdminPort;
import ir.dotin.platform.pangaea.workflow.api.admin.WorkflowRunView;
import ir.dotin.platform.pangaea.workflow.api.model.RunState;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbOutboxReemitPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbReconStatePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconReemitOutcome;

import lombok.RequiredArgsConstructor;

/**
 * Convergence action for the {@code facility-state} reconciliation type. Converges a divergent facility by re-driving
 * the stored Nova outbox row (Nova→FCB lag/orphan) or re-driving a Nova inbox row / asking FCB to re-emit (FCB→Nova
 * lag) — never by minting a new id or re-running a business handler.
 *
 * <p>Safety invariants honoured here:
 *
 * <ul>
 *   <li>INV-15: {@link #tierFor} returns {@code OPERATOR_GATED} for every revoke/cancel or money/terminal path, in
 *       code.
 *   <li>INV-6: {@link #converge} re-checks for an in-flight workflow run that owns the facility (via the facility's
 *       outbox-row correlation ids) and defers if one is mid-flight — defeating a check-and-act race with the
 *       originating flow.
 *   <li>INV-5: picks the EARLIEST not-yet-applied forward outbox event (lowest sequence number).
 *   <li>INV-3: status-aware lever — branches on the stored row's {@link MessageStatus}.
 * </ul>
 *
 * <p>Every FCB/admin call is wrapped so a thrown exception degrades to {@code retryLater} (the pangaea driver also
 * isolates exceptions, but this is defensive).
 */
@Component
@RequiredArgsConstructor
public class FacilityConvergenceAction implements ConvergenceAction {

    private static final Logger log = LoggerFactory.getLogger(FacilityConvergenceAction.class);

    private static final String AGGREGATE_TYPE = "TradeLoanFacility";
    private static final int OUTBOX_SCAN_PAGE = 200;
    private static final int INBOX_SCAN_PAGE = 200;

    private final OutboxAdminPort outboxAdminPort;
    private final InboxAdminPort inboxAdminPort;
    private final WorkflowAdminPort workflowAdminPort;
    private final FcbReconStatePort fcbReconStatePort;
    private final FcbOutboxReemitPort fcbOutboxReemitPort;
    private final FacilityReconReadPort readPort;
    private final ReconciliationSourceProperties properties;

    private final FacilityRootCauseClassifier classifier = new FacilityRootCauseClassifier();
    private final FacilityDossierBuilder dossierBuilder = new FacilityDossierBuilder();

    @Override
    public ReconciliationType type() {
        return FacilityReconciliationSource.TYPE;
    }

    // ════════════════════════════════════════ Tier (INV-15, in code) ════════════════════════════════════════

    @Override
    public AutonomyTier tierFor(OpaqueKey key, Divergence divergence) {
        Optional<FacilityReconRow> row = readPort.findById(key.value());
        if (row.isEmpty()) {
            // can't see the Nova side → don't auto-drive
            return AutonomyTier.OPERATOR_GATED;
        }
        FacilityStatus novaStatus = row.get().status();

        // Revoke/cancel path or money/terminal path → operator-gated.
        if (novaStatus == FacilityStatus.CANCELLED
                || FacilityReconMapping.isTerminal(novaStatus)
                || FacilityReconMapping.isMoneyState(novaStatus)) {
            return AutonomyTier.OPERATOR_GATED;
        }
        // Forward create/approve/contract/collateral LAGGING/ORPHAN are auto-safe.
        return AutonomyTier.AUTO_SAFE;
    }

    // ════════════════════════════════════════ Converge ════════════════════════════════════════

    @Override
    public ConvergeOutcome converge(
            OpaqueKey key, Divergence divergence, @Nullable String correlationId, boolean operatorForced) {
        String facilityId = key.value();
        try {
            // Load the facility's outbox rows once — used for the workflow guard AND the status-aware lever.
            List<OutboxRecordView> facilityRows = loadFacilityOutboxRows(facilityId);

            List<String> correlationIds = facilityRows.stream()
                    .map(OutboxRecordView::correlationId)
                    .filter(Objects::nonNull)
                    .map(UUID::toString)
                    .distinct()
                    .toList();

            // (1) WORKFLOW GUARD (INV-6). When the facility's originating workflow correlation is known, run the WHOLE
            // re-check-and-converge UNDER that run's per-correlation lock so the convergence cannot race the workflow
            // orchestrator (check-and-act atomicity); the in-flight re-check runs inside the lock to defeat TOCTOU. A
            // contended lock (run actively processed elsewhere) -> retryLater. When no correlation is resolvable, fall
            // back to a recency defer (a freshly-modified facility is likely mid-flow).
            if (correlationIds.isEmpty()) {
                ConvergeOutcome recency = deferIfRecentlyModified(facilityId);
                if (recency != null) {
                    return recency;
                }
                return convergeGuardedBody(facilityId, facilityRows, divergence, correlationIds, operatorForced);
            }
            return workflowAdminPort
                    .runUnderCorrelationGuard(
                            correlationIds.get(0),
                            () -> convergeGuardedBody(
                                    facilityId, facilityRows, divergence, correlationIds, operatorForced))
                    .orElse(ConvergeOutcome.retryLater("saga-locked"));
        } catch (RuntimeException e) {
            log.warn("converge() failed for facility {} — retryLater", facilityId, e);
            return ConvergeOutcome.retryLater("converge-exception");
        }
    }

    /**
     * Convergence body, run under the workflow lock when a correlation is known (INV-6). Re-checks for an in-flight
     * workflow run owning any of the facility's correlations inside the lock (TOCTOU), then applies terminal-dominance,
     * the money gate, and the verdict-directed lever.
     */
    private ConvergeOutcome convergeGuardedBody(
            String facilityId,
            List<OutboxRecordView> facilityRows,
            Divergence divergence,
            List<String> correlationIds,
            boolean operatorForced) {
        // In-flight workflow re-check (inside the lock): never fight an originating flow or its compensation.
        for (String corrId : correlationIds) {
            for (WorkflowRunView run : workflowAdminPort.findByCorrelation(corrId)) {
                RunState state = run.state();
                if (state == RunState.EXECUTING || state == RunState.COMPENSATING) {
                    return ConvergeOutcome.retryLater("saga-in-flight");
                }
            }
        }

        // (2) TERMINAL DOMINANCE: re-read Nova status.
        Optional<FacilityReconRow> novaRow = readPort.findById(facilityId);
        if (novaRow.isEmpty()) {
            return ConvergeOutcome.retryLater("nova-missing");
        }
        FacilityStatus novaStatus = novaRow.get().status();
        List<String> forwardEventUids = forwardEventUids(facilityRows);
        if (FacilityReconMapping.isTerminal(novaStatus)) {
            Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId, forwardEventUids);
            if (fcbResult.isFailure()) {
                return ConvergeOutcome.retryLater("fcb-unreachable");
            }
            ReconLoanFileState fcb = fcbResult.unwrap();
            if (!fcb.reachable()) {
                return ConvergeOutcome.retryLater("fcb-unreachable");
            }
            boolean aligned = !fcb.exists() || FacilityReconMapping.isFcbRevoked(fcb.fileStatus());
            return aligned
                    ? ConvergeOutcome.notApplicable("terminal-aligned")
                    : classifyDivergence(novaStatus, fcb, facilityRows, correlationIds);
        }

        // Defense-in-depth (INV-15): re-assert the money gate locally on the freshly re-read status, so a money-moving
        // facility (PARTIALLY/FULLY_DISBURSED — non-terminal, slips past the terminal check) can never be
        // auto-re-driven
        // even if it reached here via an operator-forced path or the driver gate were ever loosened.
        if (FacilityReconMapping.isMoneyState(novaStatus)) {
            Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId, forwardEventUids);
            if (fcbResult.isFailure()) {
                return ConvergeOutcome.retryLater("fcb-unreachable");
            }
            ReconLoanFileState fcb = fcbResult.unwrap();
            if (!fcb.reachable()) {
                return ConvergeOutcome.retryLater("fcb-unreachable");
            }
            return classifyDivergence(novaStatus, fcb, facilityRows, correlationIds);
        }

        // (3) / (4) verdict-directed lever.
        return switch (divergence.verdict()) {
            case ORPHAN, LAGGING ->
                convergeNovaToFcb(facilityId, facilityRows, novaStatus, correlationIds, operatorForced);
            case ALIGNED, UNKNOWN -> ConvergeOutcome.notApplicable("not-divergent");
        };
    }

    // ════════════════════════════════════════ Remediate (operator-approved REPLAY_FORWARD) ════════════════════════

    @Override
    public RemediateOutcome remediate(
            OpaqueKey key,
            Divergence divergence,
            RemediationKind approvedKind,
            String dossierHash,
            String operatorReason,
            @Nullable String correlationId) {
        if (!properties.isReplayForwardEnabled()) {
            return RemediateOutcome.notEnabled("replay-forward-disabled");
        }
        if (approvedKind != RemediationKind.REPLAY_FORWARD) {
            return RemediateOutcome.rejected("unsupported-kind:" + approvedKind);
        }

        String facilityId = key.value();
        try {
            List<OutboxRecordView> facilityRows = loadFacilityOutboxRows(facilityId);
            List<String> correlationIds = facilityRows.stream()
                    .map(OutboxRecordView::correlationId)
                    .filter(Objects::nonNull)
                    .map(UUID::toString)
                    .distinct()
                    .toList();

            String guardKey = correlationIds.isEmpty()
                    ? (correlationId == null ? facilityId : correlationId)
                    : correlationIds.get(0);

            return workflowAdminPort
                    .runUnderCorrelationGuard(
                            guardKey, () -> remediateGuardedBody(facilityId, facilityRows, correlationIds, dossierHash))
                    .orElse(RemediateOutcome.rejected("saga-locked"));
        } catch (RuntimeException e) {
            log.warn("remediate() failed for facility {} — rejected", facilityId, e);
            return RemediateOutcome.rejected("remediate-exception");
        }
    }

    private RemediateOutcome remediateGuardedBody(
            String facilityId, List<OutboxRecordView> facilityRows, List<String> correlationIds, String dossierHash) {
        for (String corrId : correlationIds) {
            for (WorkflowRunView run : workflowAdminPort.findByCorrelation(corrId)) {
                RunState state = run.state();
                if (state == RunState.EXECUTING || state == RunState.COMPENSATING) {
                    return RemediateOutcome.rejected("saga-in-flight");
                }
            }
        }

        Optional<FacilityReconRow> novaRow = readPort.findById(facilityId);
        if (novaRow.isEmpty()) {
            return RemediateOutcome.rejected("nova-missing");
        }
        FacilityStatus novaStatus = novaRow.get().status();

        List<OutboxRecordView> forwardRows = forwardRows(facilityRows);
        Result<ReconLoanFileState> fcbResult =
                fcbReconStatePort.loadReconState(facilityId, forwardEventUids(facilityRows));
        if (fcbResult.isFailure()) {
            return RemediateOutcome.rejected("fcb-unreachable");
        }
        ReconLoanFileState fcb = fcbResult.unwrap();
        if (!fcb.reachable()) {
            return RemediateOutcome.rejected("fcb-unreachable");
        }

        FacilityClassification c = classifier.classify(classifierInput(fcb, novaStatus, forwardRows));

        // TOCTOU re-check under the lock: recompute the maker-checker hash from the FRESH signals and reject if it
        // moved since the operator approved (the hash folds the per-uid peer signal, not a clock — LN-59513).
        String fresh =
                FacilityDossierBuilder.computeHash(c.rootCause(), fcb, novaStatus, forwardRows, c.recommendedKind());
        if (!fresh.equals(dossierHash)) {
            return RemediateOutcome.dossierStale("state-moved");
        }

        if (c.rootCause() != RootCause.FCB_APPLY_LOST
                || c.confidence() != Confidence.HIGH
                || c.safetyTier() == SafetyTier.MANUAL_ONLY) {
            return RemediateOutcome.rejected("not-replayable");
        }

        // Money-safety (INV-15), defense-in-depth with FCB's rank-gated effect-aware re-apply: re-drive ONLY the
        // forward
        // events FCB has not yet applied — those whose FCB target rank is strictly above FCB's current file rank. An
        // FCB-absent apply-lost orphan (currentRank = -1) re-drives the whole forward chain from CREATE; a
        // partially-applied file re-drives only the missing tail, so a re-driven REPLAY_FORWARD can never re-post an
        // already-applied disbursement (double-grant) even if FCB's eventUid dedup ever lapses.
        int currentRank = fcb.exists() ? FacilityReconMapping.fcbRank(fcb.fileStatus()) : -1;
        List<UUID> eventIds = forwardRows.stream()
                .filter(row -> row.status() == MessageStatus.PROCESSED)
                .filter(row -> row.eventId() != null)
                .filter(row -> FacilityReconMapping.fcbRankForEvent(row.eventType()) > currentRank)
                .sorted(Comparator.comparingLong(
                        row -> row.sequenceNumber() == null ? Long.MAX_VALUE : row.sequenceNumber()))
                .map(OutboxRecordView::eventId)
                .toList();
        if (eventIds.isEmpty()) {
            return RemediateOutcome.rejected("no-replayable-events");
        }

        outboxAdminPort.republish(
                new OutboxRepublishCommand(eventIds, AGGREGATE_TYPE, null, null, null, null, eventIds.size()));
        return RemediateOutcome.accepted("re-driven:" + eventIds.size());
    }

    // ════════════════════════════════════════ Divergence classification ════════════════════════════════════════

    private ConvergeOutcome classifyDivergence(
            FacilityStatus novaStatus,
            ReconLoanFileState fcb,
            List<OutboxRecordView> facilityRows,
            List<String> correlationIds) {
        List<OutboxRecordView> forwardRows = forwardRows(facilityRows);
        FacilityClassification classification = classifier.classify(classifierInput(fcb, novaStatus, forwardRows));

        if (classification.rootCause() == RootCause.FCB_LAG) {
            return ConvergeOutcome.retryLater("fcb-lag");
        }

        return ConvergeOutcome.needsOperator(
                buildDossier(novaStatus, fcb, forwardRows, correlationIds, classification));
    }

    private OperatorDossier buildDossier(
            FacilityStatus novaStatus,
            ReconLoanFileState fcb,
            List<OutboxRecordView> forwardRows,
            List<String> correlationIds,
            FacilityClassification classification) {
        String workflowState = workflowStateSummary(correlationIds);
        return dossierBuilder.build(novaStatus, fcb, forwardRows, workflowState, classification, null);
    }

    private static List<OutboxRecordView> forwardRows(List<OutboxRecordView> facilityRows) {
        return facilityRows.stream()
                .filter(row -> FacilityReconMapping.fcbRankForEvent(row.eventType()) >= 0)
                .toList();
    }

    /** Forward event uids (Nova outbox eventId = the wire {@code eventUid} = FCB idempotency/DLT key) for the probe. */
    private static List<String> forwardEventUids(List<OutboxRecordView> facilityRows) {
        return facilityRows.stream()
                .filter(row -> FacilityReconMapping.fcbRankForEvent(row.eventType()) >= 0)
                .map(OutboxRecordView::eventId)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .toList();
    }

    private static FacilityRootCauseClassifier.ClassifierInput classifierInput(
            ReconLoanFileState fcb, FacilityStatus novaStatus, List<OutboxRecordView> forwardRows) {
        return new FacilityRootCauseClassifier.ClassifierInput(
                fcb.exists(),
                fcb.fileStatus(),
                novaStatus,
                forwardRows,
                peerSignalMap(fcb),
                fcb.dltPresentForFacility(),
                null);
    }

    private static Map<String, EventPeerSignal> peerSignalMap(ReconLoanFileState fcb) {
        Map<String, EventPeerSignal> byUid = new HashMap<>();
        for (EventPeerSignal signal : fcb.peerSignals()) {
            if (signal.eventUid() != null) {
                byUid.put(signal.eventUid(), signal);
            }
        }
        return byUid;
    }

    private static boolean anyForwardInFlight(List<OutboxRecordView> facilityRows) {
        return facilityRows.stream()
                .filter(row -> FacilityReconMapping.fcbRankForEvent(row.eventType()) >= 0)
                .map(OutboxRecordView::status)
                .anyMatch(status -> status == MessageStatus.PENDING
                        || status == MessageStatus.RETRYING
                        || status == MessageStatus.PROCESSING);
    }

    // ════════════════════════════════════════ Classify (no convergence) ════════════════════════════════════════

    @Override
    public Optional<OperatorDossier> classify(OpaqueKey key, Divergence divergence) {
        String facilityId = key.value();
        try {
            Optional<FacilityReconRow> novaRow = readPort.findById(facilityId);
            if (novaRow.isEmpty()) {
                return Optional.empty();
            }
            List<OutboxRecordView> facilityRows = loadFacilityOutboxRows(facilityId);
            Result<ReconLoanFileState> fcbResult =
                    fcbReconStatePort.loadReconState(facilityId, forwardEventUids(facilityRows));
            if (fcbResult.isFailure()) {
                return Optional.empty();
            }
            ReconLoanFileState fcb = fcbResult.unwrap();
            if (!fcb.reachable()) {
                return Optional.empty();
            }

            FacilityStatus novaStatus = novaRow.get().status();
            List<String> correlationIds = facilityRows.stream()
                    .map(OutboxRecordView::correlationId)
                    .filter(Objects::nonNull)
                    .map(UUID::toString)
                    .distinct()
                    .toList();
            List<OutboxRecordView> forwardRows = forwardRows(facilityRows);

            FacilityClassification classification = classifier.classify(classifierInput(fcb, novaStatus, forwardRows));
            return Optional.of(buildDossier(novaStatus, fcb, forwardRows, correlationIds, classification));
        } catch (RuntimeException e) {
            log.warn("classify() failed for facility {} — no dossier", facilityId, e);
            return Optional.empty();
        }
    }

    private String workflowStateSummary(List<String> correlationIds) {
        List<String> states = correlationIds.stream()
                .flatMap(corrId -> workflowAdminPort.findByCorrelation(corrId).stream())
                .map(WorkflowRunView::state)
                .filter(Objects::nonNull)
                .map(RunState::name)
                .distinct()
                .toList();
        return states.isEmpty() ? "none" : String.join(",", states);
    }

    // ════════════════════════════════════════ (1) Workflow guard fallback ════════════════════════════════════════

    /**
     * INV-6 fallback when no workflow correlation is resolvable: defer a facility modified less than 30 minutes ago.
     */
    private @Nullable ConvergeOutcome deferIfRecentlyModified(String facilityId) {
        Optional<FacilityReconRow> row = readPort.findById(facilityId);
        if (row.isPresent()) {
            long ageMs = System.currentTimeMillis() - row.get().modifiedAtEpochMs();
            if (row.get().modifiedAtEpochMs() > 0 && ageMs < 30 * 60 * 1000L) {
                return ConvergeOutcome.retryLater("recently-modified");
            }
        }
        return null;
    }

    // ════════════════════════════════════════ (3) Nova → FCB lever (INV-3, INV-5) ════════════════════════════════

    private ConvergeOutcome convergeNovaToFcb(
            String facilityId,
            List<OutboxRecordView> facilityRows,
            FacilityStatus novaStatus,
            List<String> correlationIds,
            boolean operatorForced) {

        // Detection-settle replacement (LN-59513): a facility whose forward outbox row is still in-flight on the Nova
        // side (PENDING/RETRYING/PROCESSING — not yet broker-acked) is mid-publish, not a premature ORPHAN. Defer on
        // the SIGNAL, not a wall-clock settling floor.
        if (anyForwardInFlight(facilityRows)) {
            return ConvergeOutcome.retryLater("outbox-in-flight");
        }

        // Re-drive the EARLIEST forward event FCB has NOT yet applied (INV-5), judged against FCB's CURRENT file status
        // — not merely the earliest stored event (which FCB may already have → an idempotent no-op that never closes a
        // LAGGING gap). So read FCB's current rank (and the peer signals) first.
        Result<ReconLoanFileState> fcbResult =
                fcbReconStatePort.loadReconState(facilityId, forwardEventUids(facilityRows));
        if (fcbResult.isFailure()) {
            return ConvergeOutcome.retryLater("fcb-unreachable");
        }
        ReconLoanFileState fcb = fcbResult.unwrap();
        if (!fcb.reachable()) {
            return ConvergeOutcome.retryLater("fcb-unreachable");
        }
        int currentRank = fcb.exists() ? FacilityReconMapping.fcbRank(fcb.fileStatus()) : -1;

        List<OutboxRecordView> step = earliestMissingStep(facilityRows, currentRank);
        if (step.isEmpty()) {
            // FCB is at/ahead of every stored forward event → not a Nova→FCB forward lag; try the FCB→Nova lever.
            return convergeFcbToNova(facilityId, facilityRows);
        }

        // Re-drive EVERY event of the earliest-missing step, not just one (D11): a single forward step can emit several
        // outbox events at the same FCB rank (e.g. the final tranche emits both IRREGULAR_TRANCHE_DISBURSED and
        // FULLY_DISBURSED). Nova cannot know which one FCB actually dispatches, so re-drive them all — FCB applies the
        // grant-triggering event (IRREGULAR_TRANCHE) and ignores the rest. INV-3 status-aware lever per event.
        List<UUID> processedIds = new ArrayList<>();
        boolean anyDeadLetter = false;
        boolean anyInProgress = false;
        for (OutboxRecordView event : step) {
            UUID eventId = event.eventId();
            MessageStatus status = event.status();
            if (eventId == null || status == null) {
                continue;
            }
            switch (status) {
                case PROCESSED -> processedIds.add(eventId);
                case DEAD_LETTER -> {
                    outboxAdminPort.manualRetry(AGGREGATE_TYPE, eventId);
                    anyDeadLetter = true;
                }
                case FAILED, RETRYING, PENDING, PROCESSING -> anyInProgress = true;
            }
        }

        // The earliest-missing step is entirely PROCESSED on the Nova side (broker-acked) yet FCB has not applied it.
        // Classify on FCB's peer signal: a NON-re-drivable terminal rejection (DLT BUSINESS/POISON/PERMANENT) must
        // escalate to an operator immediately rather than re-drive a doomed event forever; a genuine apply-lost
        // (COMPLETED/transient-exhausted) or a still-in-transit lag is re-driven below (the FCB effect-aware re-apply
        // converges it, and a never-converging row is escalated by the generic sweep's divergent-age backstop, not a
        // clock here). An operator-forced converge IS the maker-checker deciding to re-drive: skip the escalation.
        if (!operatorForced && !anyInProgress && !processedIds.isEmpty()) {
            ConvergeOutcome escalation = escalateIfTerminalReject(novaStatus, fcb, facilityRows, correlationIds);
            if (escalation != null) {
                return escalation;
            }
        }

        if (!processedIds.isEmpty()) {
            long republished = outboxAdminPort.republish(new OutboxRepublishCommand(
                    processedIds, AGGREGATE_TYPE, null, null, null, null, processedIds.size()));
            if (republished > 0) {
                // D12: the Nova→FCB corridor is asynchronous — republish only re-enqueues the stored event onto the
                // outbox→broker path; FCB consumes and applies it out-of-band (seconds later). We CANNOT synchronously
                // confirm alignment here, so return retryLater ("re-driven, re-confirm later"): the driver returns the
                // row to OPEN behind the backoff WITHOUT counting a failed attempt (INV-11). Returning Converged would
                // make the driver re-probe immediately, find FCB not-yet-caught-up, and mislabel the in-flight re-drive
                // as "still divergent after converge" (false attempt burn + false NEEDS_OPERATOR + operator-facing
                // 500).
                // The next detection sweep resolves the row once the probe observes ALIGNED.
                return ConvergeOutcome.retryLater("re-driven-outbox:" + republished);
            }
        }
        if (anyDeadLetter) {
            // Async re-drive (outbox poller re-sends the DEAD_LETTER row) — see D12 above: defer, don't claim
            // Converged.
            return ConvergeOutcome.retryLater("re-driven-dead-letter");
        }
        if (anyInProgress) {
            return ConvergeOutcome.retryLater("outbox-in-progress");
        }
        return ConvergeOutcome.retryLater("republish-noop");
    }

    /**
     * Escalate to {@code NEEDS_OPERATOR} ONLY when the forward step's peer signal is a non-re-drivable terminal FCB
     * rejection ({@code FCB_BUSINESS_REJECT} — DLT BUSINESS/POISON/PERMANENT): a replay cannot fix it, so it must not
     * re-drive forever. Returns {@code null} for an apply-lost or lag classification (the caller re-drives; a genuine
     * never-converging row is escalated by the generic sweep's divergent-age backstop). Side-effect-free, no clock.
     */
    private @Nullable ConvergeOutcome escalateIfTerminalReject(
            FacilityStatus novaStatus,
            ReconLoanFileState fcb,
            List<OutboxRecordView> facilityRows,
            List<String> correlationIds) {
        List<OutboxRecordView> forwardRows = forwardRows(facilityRows);
        FacilityClassification classification = classifier.classify(classifierInput(fcb, novaStatus, forwardRows));
        if (classification.rootCause() != RootCause.FCB_BUSINESS_REJECT) {
            return null;
        }
        return ConvergeOutcome.needsOperator(
                buildDossier(novaStatus, fcb, forwardRows, correlationIds, classification));
    }

    /**
     * All stored forward outbox rows comprising the EARLIEST step FCB has not yet applied — every row whose FCB target
     * rank equals the minimum rank still above FCB's current file-status rank (INV-5). A single forward step can emit
     * multiple events at the same rank (a final tranche emits both IRREGULAR_TRANCHE_DISBURSED and FULLY_DISBURSED);
     * returning the whole step lets FCB apply the event it dispatches and ignore the rest (D11). Filtering by rank (not
     * just sequence) is what makes a LAGGING facility re-drive the missing step instead of an already-applied CREATE;
     * an ORPHAN ({@code fcbCurrentRank == -1}) naturally yields the CREATE step first.
     */
    private static List<OutboxRecordView> earliestMissingStep(List<OutboxRecordView> rows, int fcbCurrentRank) {
        OptionalInt minMissingRank = rows.stream()
                .filter(r -> r.status() != null && r.eventId() != null)
                .mapToInt(r -> FacilityReconMapping.fcbRankForEvent(r.eventType()))
                .filter(rank -> rank > fcbCurrentRank)
                .min();
        if (minMissingRank.isEmpty()) {
            return List.of();
        }
        int step = minMissingRank.getAsInt();
        return rows.stream()
                .filter(r -> r.status() != null && r.eventId() != null)
                .filter(r -> FacilityReconMapping.fcbRankForEvent(r.eventType()) == step)
                .sorted(Comparator.comparingLong(
                        r -> r.sequenceNumber() == null ? Long.MAX_VALUE : r.sequenceNumber()))
                .toList();
    }

    // ════════════════════════════════════════ (4) FCB → Nova lever (INV-7) ════════════════════════════════

    private ConvergeOutcome convergeFcbToNova(String facilityId, List<OutboxRecordView> facilityRows) {
        // (4a) First, try to re-drive a DEAD_LETTERED Nova inbox row that belongs to this facility's flow (matched by
        // correlation id) when its messageType is redrivable (INV-7).
        ConvergeOutcome inboxOutcome = redriveDeadLetteredInbox(facilityRows);
        if (inboxOutcome != null) {
            return inboxOutcome;
        }

        // (4b) Otherwise FCB never emitted the event Nova is waiting for → ask FCB to re-emit. State-only probe (no
        // forward-event signals needed for the FCB→Nova re-emit lever): pass no uids so FCB skips the signal gather.
        Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId, null);
        if (fcbResult.isFailure()) {
            return ConvergeOutcome.retryLater("fcb-unreachable");
        }
        ReconLoanFileState fcb = fcbResult.unwrap();
        if (!fcb.reachable()) {
            return ConvergeOutcome.retryLater("fcb-unreachable");
        }

        Result<ReconReemitOutcome> reemit = fcbOutboxReemitPort.reemitOutbox(facilityId, fcb.outboxRef());
        if (reemit.isFailure()) {
            return ConvergeOutcome.retryLater("fcb-reemit-failed");
        }
        ReconReemitOutcome outcome = reemit.unwrap();
        // Async re-drive (FCB re-emits → Nova inbox consumes out-of-band) — see D12: defer, don't claim Converged.
        return outcome.reemittedCount() > 0
                ? ConvergeOutcome.retryLater("re-driven-fcb-reemit:" + outcome.reemittedCount())
                : ConvergeOutcome.retryLater("fcb-nothing-to-reemit");
    }

    /**
     * Scans DEAD_LETTERED Nova inbox rows and re-drives one whose correlation id belongs to this facility's flow and
     * whose messageType is redrivable (INV-7). Returns {@code null} when no such inbox row exists (so the caller falls
     * back to asking FCB to re-emit).
     */
    private @Nullable ConvergeOutcome redriveDeadLetteredInbox(List<OutboxRecordView> facilityRows) {
        Set<String> facilityCorrelations = facilityRows.stream()
                .map(OutboxRecordView::correlationId)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .collect(Collectors.toSet());
        if (facilityCorrelations.isEmpty()) {
            return null;
        }

        // Prefer re-driving a redrivable DEAD_LETTERED row; only escalate to NEEDS_OPERATOR if the full bounded scan
        // found a correlated DEAD_LETTERED row that is NOT redrivable and no redrivable one. A correlated row that is
        // not dead-lettered (e.g. already PROCESSED) must be skipped, never escalated (INV-7).
        boolean sawNonRedrivableDeadLetter = false;
        UUID cursor = null;
        for (int page = 0; page < 50; page++) {
            InboxAdminPage deadLetters = inboxAdminPort.findDeadLetters(cursor, INBOX_SCAN_PAGE);
            for (InboxAdminItem item : deadLetters.content()) {
                String corr = item.correlationId();
                if (corr == null || !facilityCorrelations.contains(corr)) {
                    continue;
                }
                if (item.status() != InboxStatus.DEAD_LETTERED) {
                    continue;
                }
                if (FacilityReconMapping.REDRIVABLE_INBOX_OPS.contains(item.messageType())) {
                    return interpretInboxRetry(inboxAdminPort.manualRetry(item.id()));
                }
                sawNonRedrivableDeadLetter = true;
            }
            if (!deadLetters.hasNext() || deadLetters.nextCursor() == null) {
                break;
            }
            cursor = deadLetters.nextCursor();
        }
        return sawNonRedrivableDeadLetter ? ConvergeOutcome.needsOperator("inbox-op-not-redrivable") : null;
    }

    private static ConvergeOutcome interpretInboxRetry(InboxManualRetryOutcome outcome) {
        return switch (outcome) {
            case InboxManualRetryOutcome.Requeued requeued -> {
                InboxAdminDetail detail = requeued.message();
                // Async re-drive (inbox dispatcher re-processes out-of-band) — see D12: defer, don't claim Converged.
                yield ConvergeOutcome.retryLater("inbox-redriven:" + detail.id());
            }
            case InboxManualRetryOutcome.Rejected _ -> ConvergeOutcome.retryLater("inbox-retry-rejected");
            case InboxManualRetryOutcome.NotFound _ -> ConvergeOutcome.retryLater("inbox-not-found");
        };
    }

    // ════════════════════════════════════════ Outbox row lookup ════════════════════════════════════════

    /** Scans the outbox for rows of this aggregate type and filters to the facility's aggregate id. */
    private List<OutboxRecordView> loadFacilityOutboxRows(String facilityId) {
        UUID aggregateId;
        try {
            aggregateId = UUID.fromString(facilityId);
        } catch (IllegalArgumentException e) {
            return List.of();
        }

        List<OutboxRecordView> matched = new ArrayList<>();
        UUID cursor = null;
        // Bounded scan: walk pages of this aggregate type, collecting rows for the target aggregate id.
        for (int page = 0; page < 50; page++) {
            OutboxRecordPage result = outboxAdminPort.search(
                    new OutboxAdminCriteria(null, AGGREGATE_TYPE, null, cursor, OUTBOX_SCAN_PAGE));
            for (OutboxRecordView view : result.content()) {
                if (aggregateId.equals(view.aggregateId())) {
                    matched.add(view);
                }
            }
            if (!result.hasNext() || result.nextCursor() == null) {
                break;
            }
            cursor = result.nextCursor();
        }
        return matched;
    }
}
