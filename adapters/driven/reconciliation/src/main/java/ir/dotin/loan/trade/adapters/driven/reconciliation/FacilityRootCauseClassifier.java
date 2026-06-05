package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.outbox.api.MessageStatus;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordView;
import ir.dotin.platform.pangaea.reconciliation.api.model.Confidence;
import ir.dotin.platform.pangaea.reconciliation.api.model.RemediationKind;
import ir.dotin.platform.pangaea.reconciliation.api.model.RootCause;
import ir.dotin.platform.pangaea.reconciliation.api.model.SafetyTier;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;

final class FacilityRootCauseClassifier {

    record ClassifierInput(
            boolean fcbExists,
            @Nullable String fcbFileStatus,
            FacilityStatus novaStatus,
            List<OutboxRecordView> forwardOutboxRows,
            boolean graceElapsed,
            @Nullable String fcbBusinessError) {

        ClassifierInput {
            Objects.requireNonNull(novaStatus, "novaStatus");
            forwardOutboxRows = List.copyOf(forwardOutboxRows);
        }
    }

    FacilityClassification classify(ClassifierInput input) {
        boolean money = FacilityReconMapping.isMoneyState(input.novaStatus());
        List<String> evidence = new ArrayList<>();
        evidence.add("fcb.exists=" + input.fcbExists());
        evidence.add("money=" + money);

        if (input.fcbExists()) {
            return classifyExisting(input, evidence);
        }
        return classifyAbsent(input, money, evidence);
    }

    private FacilityClassification classifyExisting(ClassifierInput input, List<String> evidence) {
        int expectedRank = FacilityReconMapping.fcbRank(FacilityReconMapping.expectedFcbFileStatus(input.novaStatus()));
        int actualRank = FacilityReconMapping.fcbRank(input.fcbFileStatus());
        evidence.add("fcb.expectedRank=" + expectedRank);
        evidence.add("fcb.actualRank=" + actualRank);

        if (expectedRank != actualRank) {
            return finalize(
                    RootCause.PARTIAL_APPLY,
                    Confidence.MEDIUM,
                    SafetyTier.MANUAL_ONLY,
                    RemediationKind.MANUAL_DATA_FIX,
                    evidence);
        }
        return finalize(RootCause.UNKNOWN, Confidence.LOW, SafetyTier.MANUAL_ONLY, RemediationKind.NONE, evidence);
    }

    private FacilityClassification classifyAbsent(ClassifierInput input, boolean money, List<String> evidence) {
        evidence.add("grace.elapsed=" + input.graceElapsed());

        if (!input.graceElapsed()) {
            return finalize(RootCause.FCB_LAG, Confidence.HIGH, SafetyTier.AUTO_SAFE, RemediationKind.NONE, evidence);
        }

        if (input.fcbBusinessError() != null) {
            evidence.add("fcb.businessError=present");
            return finalize(
                    RootCause.FCB_BUSINESS_REJECT,
                    Confidence.HIGH,
                    SafetyTier.MANUAL_ONLY,
                    RemediationKind.MANUAL_DATA_FIX,
                    evidence);
        }

        long processedForward = input.forwardOutboxRows().stream()
                .filter(row -> FacilityReconMapping.fcbRankForEvent(row.eventType()) >= 0)
                .filter(row -> row.status() == MessageStatus.PROCESSED)
                .count();
        evidence.add("outbox.forward.processed=" + processedForward);
        evidence.add("outbox.forward.count=" + input.forwardOutboxRows().size());

        if (processedForward >= 1) {
            return finalize(
                    RootCause.FCB_APPLY_LOST,
                    Confidence.HIGH,
                    money ? SafetyTier.MONEY_GATED : SafetyTier.AUTO_SAFE,
                    RemediationKind.REPLAY_FORWARD,
                    evidence);
        }

        if (input.forwardOutboxRows().isEmpty()) {
            return finalize(
                    RootCause.NOVA_PHANTOM,
                    Confidence.HIGH,
                    SafetyTier.MANUAL_ONLY,
                    RemediationKind.REVERSE_NOVA,
                    evidence);
        }

        return finalize(RootCause.UNKNOWN, Confidence.LOW, SafetyTier.MANUAL_ONLY, RemediationKind.NONE, evidence);
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
}
