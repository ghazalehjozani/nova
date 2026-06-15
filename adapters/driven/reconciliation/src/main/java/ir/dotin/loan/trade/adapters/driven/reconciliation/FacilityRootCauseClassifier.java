package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.outbox.api.MessageStatus;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordView;
import ir.dotin.platform.pangaea.reconciliation.api.model.Confidence;
import ir.dotin.platform.pangaea.reconciliation.api.model.RemediationKind;
import ir.dotin.platform.pangaea.reconciliation.api.model.RootCause;
import ir.dotin.platform.pangaea.reconciliation.api.model.SafetyTier;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal;

/**
 * Roots a facility divergence in a cause from DURABLE PEER SIGNALS — not a wall clock (LN-59513). The discriminator
 * between {@code FCB_APPLY_LOST} (FCB saw the forward event and the effect is not present → re-drivable) and
 * {@code FCB_LAG} (still in transit / not yet consumed → self-heals) is FCB's per-event idempotency state and
 * dead-letter record, joined to the Nova outbox row by {@code eventUid}. Apply-lost requires a POSITIVE signal
 * (idempotency {@code COMPLETED}, or a {@code DEAD} transient-exhausted dead-letter) plus an absent/behind FCB file;
 * elapsed time is never an input. A {@code DEAD} dead-letter in a terminal category (BUSINESS / POISON / PERMANENT_*)
 * is NOT re-drivable and surfaces as {@code FCB_BUSINESS_REJECT}.
 */
final class FacilityRootCauseClassifier {

    record ClassifierInput(
            boolean fcbExists,
            @Nullable String fcbFileStatus,
            FacilityStatus novaStatus,
            List<OutboxRecordView> forwardOutboxRows,
            Map<String, EventPeerSignal> peerSignals,
            boolean dltPresentForFacility,
            @Nullable String fcbBusinessError) {

        ClassifierInput {
            Objects.requireNonNull(novaStatus, "novaStatus");
            forwardOutboxRows = List.copyOf(forwardOutboxRows);
            peerSignals = Map.copyOf(peerSignals);
        }
    }

    FacilityClassification classify(ClassifierInput input) {
        boolean money = FacilityReconMapping.isMoneyState(input.novaStatus());
        List<String> evidence = new ArrayList<>();
        evidence.add("fcb.exists=" + input.fcbExists());
        evidence.add("money=" + money);
        evidence.add("signal.dltFacility=" + input.dltPresentForFacility());

        if (input.fcbExists()) {
            return classifyExisting(input, money, evidence);
        }
        return classifyAbsent(input, money, evidence);
    }

    private FacilityClassification classifyExisting(ClassifierInput input, boolean money, List<String> evidence) {
        int expectedRank = FacilityReconMapping.fcbRank(FacilityReconMapping.expectedFcbFileStatus(input.novaStatus()));
        int actualRank = FacilityReconMapping.fcbRank(input.fcbFileStatus());
        evidence.add("fcb.expectedRank=" + expectedRank);
        evidence.add("fcb.actualRank=" + actualRank);

        if (expectedRank == actualRank) {
            return finalize(RootCause.UNKNOWN, Confidence.LOW, SafetyTier.MANUAL_ONLY, RemediationKind.NONE, evidence);
        }

        // FCB is behind Nova's expectation: classify the forward steps FCB has not yet applied by their peer signal.
        List<OutboxRecordView> missingProcessed = input.forwardOutboxRows().stream()
                .filter(row -> row.status() == MessageStatus.PROCESSED)
                .filter(row -> FacilityReconMapping.fcbRankForEvent(row.eventType()) > actualRank)
                .toList();
        SignalVerdict verdict = inspectSignals(input, missingProcessed, evidence);

        if (verdict.terminalReject() || input.fcbBusinessError() != null) {
            return finalize(
                    RootCause.FCB_BUSINESS_REJECT,
                    Confidence.HIGH,
                    SafetyTier.MANUAL_ONLY,
                    RemediationKind.MANUAL_DATA_FIX,
                    evidence);
        }
        if (verdict.applyLost()) {
            return finalize(
                    RootCause.FCB_APPLY_LOST,
                    Confidence.HIGH,
                    money ? SafetyTier.MONEY_GATED : SafetyTier.AUTO_SAFE,
                    RemediationKind.REPLAY_FORWARD,
                    evidence);
        }
        // FCB exists but is behind with no positive lost signal: a genuine partial-apply needing operator data fix
        // (an in-transit step that simply hasn't been consumed yet is handled by the convergence re-drive, not here).
        return finalize(
                RootCause.PARTIAL_APPLY,
                Confidence.MEDIUM,
                SafetyTier.MANUAL_ONLY,
                RemediationKind.MANUAL_DATA_FIX,
                evidence);
    }

    private FacilityClassification classifyAbsent(ClassifierInput input, boolean money, List<String> evidence) {
        if (input.forwardOutboxRows().isEmpty()) {
            return finalize(
                    RootCause.NOVA_PHANTOM,
                    Confidence.HIGH,
                    SafetyTier.MANUAL_ONLY,
                    RemediationKind.REVERSE_NOVA,
                    evidence);
        }

        List<OutboxRecordView> processedForward = input.forwardOutboxRows().stream()
                .filter(row -> FacilityReconMapping.fcbRankForEvent(row.eventType()) >= 0)
                .filter(row -> row.status() == MessageStatus.PROCESSED)
                .toList();
        evidence.add("outbox.forward.processed=" + processedForward.size());
        evidence.add("outbox.forward.count=" + input.forwardOutboxRows().size());

        if (processedForward.isEmpty()) {
            // Forward events exist but none is broker-acked yet → still in-flight on the Nova side: lag, not loss.
            return finalize(RootCause.FCB_LAG, Confidence.HIGH, SafetyTier.AUTO_SAFE, RemediationKind.NONE, evidence);
        }

        SignalVerdict verdict = inspectSignals(input, processedForward, evidence);

        if (verdict.terminalReject() || input.fcbBusinessError() != null) {
            return finalize(
                    RootCause.FCB_BUSINESS_REJECT,
                    Confidence.HIGH,
                    SafetyTier.MANUAL_ONLY,
                    RemediationKind.MANUAL_DATA_FIX,
                    evidence);
        }
        if (verdict.applyLost()) {
            // FCB saw the forward event (COMPLETED, or a transient-exhausted DEAD) but the file is absent →
            // confirm-without-effect = genuine apply-lost, re-drivable via REPLAY_FORWARD.
            return finalize(
                    RootCause.FCB_APPLY_LOST,
                    Confidence.HIGH,
                    money ? SafetyTier.MONEY_GATED : SafetyTier.AUTO_SAFE,
                    RemediationKind.REPLAY_FORWARD,
                    evidence);
        }
        // IN_PROGRESS, or no positive signal at all (idempotency ABSENT / FCB pre-signal version / signal gather
        // failed) → treat as lag: the convergence keeps safely re-driving, and a genuine never-seen gap is escalated to
        // an operator by the generic sweep's divergent-age backstop, never auto-remediated on a clock.
        return finalize(RootCause.FCB_LAG, Confidence.HIGH, SafetyTier.AUTO_SAFE, RemediationKind.NONE, evidence);
    }

    /**
     * Folds the peer signals of the given processed forward rows into a {@link SignalVerdict}. {@code terminalReject}
     * wins precedence (a non-re-drivable FCB rejection must not be re-driven even if an earlier step completed).
     */
    private SignalVerdict inspectSignals(ClassifierInput input, List<OutboxRecordView> rows, List<String> evidence) {
        boolean terminalReject = false;
        boolean applyLost = false;
        boolean inProgress = false;
        for (OutboxRecordView row : rows) {
            EventPeerSignal signal = input.peerSignals().get(eventUidOf(row));
            if (signal == null) {
                continue;
            }
            if (signal.dltDead()) {
                if (isTerminalDltCategory(signal.dltCategory())) {
                    terminalReject = true;
                } else {
                    applyLost = true;
                }
            }
            switch (signal.idempotencyState()) {
                case COMPLETED -> applyLost = true;
                case IN_PROGRESS -> inProgress = true;
                case ABSENT -> {
                    // no FCB record of this uid — contributes nothing positive
                }
            }
        }
        evidence.add("signal.terminalReject=" + terminalReject);
        evidence.add("signal.applyLost=" + applyLost);
        evidence.add("signal.inProgress=" + inProgress);
        return new SignalVerdict(terminalReject, applyLost, inProgress);
    }

    private static boolean isTerminalDltCategory(@Nullable String category) {
        // A DEAD dead-letter is re-drivable ONLY when it exhausted transient retries; every other category
        // (BUSINESS / POISON / PERMANENT_* / unknown) is a terminal rejection a replay cannot fix.
        if (category == null) {
            return true;
        }
        return !category.startsWith("TRANSIENT");
    }

    private static @Nullable String eventUidOf(OutboxRecordView row) {
        return row.eventId() == null ? null : row.eventId().toString();
    }

    private FacilityClassification finalize(
            RootCause rootCause,
            Confidence confidence,
            SafetyTier safetyTier,
            RemediationKind recommendedKind,
            List<String> evidence) {
        SafetyTier guardedTier = confidence == Confidence.HIGH ? safetyTier : SafetyTier.MANUAL_ONLY;
        return new FacilityClassification(rootCause, confidence, guardedTier, recommendedKind, evidence);
    }

    private record SignalVerdict(boolean terminalReject, boolean applyLost, boolean inProgress) {}
}
