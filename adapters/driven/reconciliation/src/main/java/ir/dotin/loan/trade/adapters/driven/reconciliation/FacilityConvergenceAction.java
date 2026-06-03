package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import ir.dotin.platform.pangaea.reconciliation.api.model.AutonomyTier;
import ir.dotin.platform.pangaea.reconciliation.api.model.ConvergeOutcome;
import ir.dotin.platform.pangaea.reconciliation.api.model.Divergence;
import ir.dotin.platform.pangaea.reconciliation.api.model.OpaqueKey;
import ir.dotin.platform.pangaea.reconciliation.api.model.ReconciliationType;
import ir.dotin.platform.pangaea.reconciliation.api.spi.ConvergenceAction;
import ir.dotin.platform.pangaea.saga.api.admin.SagaAdminPort;
import ir.dotin.platform.pangaea.saga.api.admin.SagaInstanceView;
import ir.dotin.platform.pangaea.saga.api.model.SagaState;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
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
 *   <li>INV-6: {@link #converge} re-checks for an in-flight saga that owns the facility (via the facility's outbox-row
 *       correlation ids) and defers if one is mid-flight — defeating a check-and-act race with the originating flow.
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
    private final SagaAdminPort sagaAdminPort;
    private final FcbReconStatePort fcbReconStatePort;
    private final FacilityReconReadPort readPort;

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
    public ConvergeOutcome converge(OpaqueKey key, Divergence divergence, @Nullable String correlationId) {
        String facilityId = key.value();
        try {
            // Load the facility's outbox rows once — used for the saga guard AND the status-aware lever.
            List<OutboxRecordView> facilityRows = loadFacilityOutboxRows(facilityId);

            // (1) SAGA GUARD (INV-6): defer if any saga owning this facility's correlation is in flight.
            ConvergeOutcome guard = sagaGuard(facilityId, facilityRows);
            if (guard != null) {
                return guard;
            }

            // (2) TERMINAL DOMINANCE: re-read Nova status.
            Optional<FacilityReconRow> novaRow = readPort.findById(facilityId);
            if (novaRow.isEmpty()) {
                return ConvergeOutcome.retryLater("nova-missing");
            }
            FacilityStatus novaStatus = novaRow.get().status();
            if (FacilityReconMapping.isTerminal(novaStatus)) {
                Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId);
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
                        : ConvergeOutcome.needsOperator("terminal-divergent");
            }

            // Defense-in-depth (INV-15): re-assert the money gate locally on the freshly re-read status, so a
            // money-moving facility (PARTIALLY/FULLY_DISBURSED — non-terminal, so it slips past the terminal check
            // above) can never be auto-re-driven even if it reached converge() via an operator-forced path or the
            // driver gate were ever loosened. tierFor() already gates it for the auto-sweep; this makes the
            // irreversible-path gate locally enforced and refactor-proof.
            if (FacilityReconMapping.isMoneyState(novaStatus)) {
                return ConvergeOutcome.needsOperator("money-state");
            }

            // (3) / (4) verdict-directed lever.
            return switch (divergence.verdict()) {
                case ORPHAN, LAGGING -> convergeNovaToFcb(facilityId, facilityRows);
                case ALIGNED, UNKNOWN -> ConvergeOutcome.notApplicable("not-divergent");
            };
        } catch (RuntimeException e) {
            log.warn("converge() failed for facility {} — retryLater", facilityId, e);
            return ConvergeOutcome.retryLater("converge-exception");
        }
    }

    // ════════════════════════════════════════ (1) Saga guard ════════════════════════════════════════

    private @Nullable ConvergeOutcome sagaGuard(String facilityId, List<OutboxRecordView> facilityRows) {
        List<String> correlationIds = facilityRows.stream()
                .map(OutboxRecordView::correlationId)
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .distinct()
                .toList();

        if (correlationIds.isEmpty()) {
            // Fallback (INV-6): can't resolve a correlation id → defer if recently modified (< 30 min).
            Optional<FacilityReconRow> row = readPort.findById(facilityId);
            if (row.isPresent()) {
                long ageMs = System.currentTimeMillis() - row.get().modifiedAtEpochMs();
                if (row.get().modifiedAtEpochMs() > 0 && ageMs < 30 * 60 * 1000L) {
                    return ConvergeOutcome.retryLater("recently-modified");
                }
            }
            return null;
        }

        for (String corrId : correlationIds) {
            List<SagaInstanceView> sagas = sagaAdminPort.findByCorrelation(corrId);
            for (SagaInstanceView saga : sagas) {
                SagaState state = saga.state();
                if (state == SagaState.PENDING || state == SagaState.EXECUTING || state == SagaState.COMPENSATING) {
                    return ConvergeOutcome.retryLater("saga-in-flight");
                }
            }
        }
        return null;
    }

    // ════════════════════════════════════════ (3) Nova → FCB lever (INV-3, INV-5) ════════════════════════════════

    private ConvergeOutcome convergeNovaToFcb(String facilityId, List<OutboxRecordView> facilityRows) {

        // Re-drive the EARLIEST forward event FCB has NOT yet applied (INV-5), judged against FCB's CURRENT file status
        // — not merely the earliest stored event (which FCB may already have → an idempotent no-op that never closes a
        // LAGGING gap). So read FCB's current rank first.
        Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId);
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

        if (!processedIds.isEmpty()) {
            long republished = outboxAdminPort.republish(
                    new OutboxRepublishCommand(processedIds, AGGREGATE_TYPE, null, null, processedIds.size()));
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
                .sorted(Comparator.comparing(r -> r.sequenceNumber() == null ? Long.MAX_VALUE : r.sequenceNumber()))
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

        // (4b) Otherwise FCB never emitted the event Nova is waiting for → ask FCB to re-emit.
        Result<ReconLoanFileState> fcbResult = fcbReconStatePort.loadReconState(facilityId);
        if (fcbResult.isFailure()) {
            return ConvergeOutcome.retryLater("fcb-unreachable");
        }
        ReconLoanFileState fcb = fcbResult.unwrap();
        if (!fcb.reachable()) {
            return ConvergeOutcome.retryLater("fcb-unreachable");
        }

        Result<ReconReemitOutcome> reemit = fcbReconStatePort.reemitOutbox(facilityId, fcb.outboxRef());
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
            OutboxRecordPage result =
                    outboxAdminPort.search(new OutboxAdminCriteria(null, AGGREGATE_TYPE, cursor, OUTBOX_SCAN_PAGE));
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
