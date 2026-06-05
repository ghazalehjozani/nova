package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ir.dotin.platform.pangaea.outbox.api.MessageStatus;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordView;
import ir.dotin.platform.pangaea.reconciliation.api.model.Confidence;
import ir.dotin.platform.pangaea.reconciliation.api.model.RemediationKind;
import ir.dotin.platform.pangaea.reconciliation.api.model.RootCause;
import ir.dotin.platform.pangaea.reconciliation.api.model.SafetyTier;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;

import static org.assertj.core.api.Assertions.assertThat;

class RemediationSafetyInvariantsTest {

    private final FacilityRootCauseClassifier classifier = new FacilityRootCauseClassifier();

    @ParameterizedTest(name = "uncertaintyForcesManualOnly[{0}]")
    @MethodSource("classifierInputSweep")
    void uncertaintyAlwaysForcesManualOnly(String name, FacilityRootCauseClassifier.ClassifierInput input) {
        FacilityClassification result = classifier.classify(input);

        if (result.confidence() != Confidence.HIGH) {
            assertThat(result.safetyTier())
                    .as("INVARIANT uncertainty=>MANUAL_ONLY for %s (confidence=%s)", name, result.confidence())
                    .isEqualTo(SafetyTier.MANUAL_ONLY);
        }
    }

    @ParameterizedTest(name = "replayOnlyWhenHighApplyLostNonManual[{0}]")
    @MethodSource("classifierInputSweep")
    void replayForwardImpliesHighConfidenceApplyLostAndNonManual(
            String name, FacilityRootCauseClassifier.ClassifierInput input) {
        FacilityClassification result = classifier.classify(input);

        if (result.recommendedKind() == RemediationKind.REPLAY_FORWARD) {
            assertThat(result.rootCause())
                    .as("INVARIANT REPLAY_FORWARD=>FCB_APPLY_LOST for %s", name)
                    .isEqualTo(RootCause.FCB_APPLY_LOST);
            assertThat(result.confidence())
                    .as("INVARIANT REPLAY_FORWARD=>confidence HIGH for %s", name)
                    .isEqualTo(Confidence.HIGH);
            assertThat(result.safetyTier())
                    .as("INVARIANT REPLAY_FORWARD=>not MANUAL_ONLY for %s", name)
                    .isNotEqualTo(SafetyTier.MANUAL_ONLY);
        }
    }

    @ParameterizedTest(name = "moneyApplyLostNeverAutoSafe[{0}]")
    @MethodSource("moneyStateSweep")
    void moneyStateClassifiedApplyLostIsMoneyGatedNeverAutoSafe(
            String name, FacilityRootCauseClassifier.ClassifierInput input) {
        FacilityClassification result = classifier.classify(input);

        if (result.rootCause() == RootCause.FCB_APPLY_LOST) {
            assertThat(result.safetyTier())
                    .as("INVARIANT money+FCB_APPLY_LOST=>MONEY_GATED (never AUTO_SAFE) for %s", name)
                    .isEqualTo(SafetyTier.MONEY_GATED);
            assertThat(result.safetyTier())
                    .as("INVARIANT money+FCB_APPLY_LOST never AUTO_SAFE for %s", name)
                    .isNotEqualTo(SafetyTier.AUTO_SAFE);
        }
    }

    static Stream<Arguments> classifierInputSweep() {
        return Stream.concat(moneyStateSweep(), nonMoneyStateSweep());
    }

    static Stream<Arguments> moneyStateSweep() {
        FacilityStatus money = FacilityStatus.FULLY_DISBURSED;
        return Stream.of(
                Arguments.of("money/absent/grace/processed", input(false, null, money, true, null, processedForward())),
                Arguments.of("money/absent/notGrace", input(false, null, money, false, null, processedForward())),
                Arguments.of(
                        "money/absent/grace/businessError",
                        input(false, null, money, true, "FCB rejected", processedForward())),
                Arguments.of("money/absent/grace/noForward", input(false, null, money, true, null, List.of())),
                Arguments.of(
                        "money/absent/grace/forwardDeadLetter",
                        input(false, null, money, true, null, deadLetterForward())),
                Arguments.of(
                        "money/exists/rankMismatch",
                        input(true, FacilityReconMapping.FCB_REQUEST_LOAN, money, true, null, processedForward())));
    }

    static Stream<Arguments> nonMoneyStateSweep() {
        FacilityStatus nonMoney = FacilityStatus.APPROVED;
        return Stream.of(
                Arguments.of(
                        "nonMoney/absent/grace/processed",
                        input(false, null, nonMoney, true, null, processedForward())),
                Arguments.of("nonMoney/absent/notGrace", input(false, null, nonMoney, false, null, processedForward())),
                Arguments.of(
                        "nonMoney/absent/grace/businessError",
                        input(false, null, nonMoney, true, "FCB rejected", processedForward())),
                Arguments.of("nonMoney/absent/grace/noForward", input(false, null, nonMoney, true, null, List.of())),
                Arguments.of(
                        "nonMoney/absent/grace/forwardDeadLetter",
                        input(false, null, nonMoney, true, null, deadLetterForward())),
                Arguments.of(
                        "nonMoney/exists/rankMismatch",
                        input(true, FacilityReconMapping.FCB_REQUEST_LOAN, nonMoney, true, null, processedForward())),
                Arguments.of(
                        "nonMoney/exists/rankMatch",
                        input(true, FacilityReconMapping.FCB_APPROVE_LOAN, nonMoney, true, null, processedForward())));
    }

    private static FacilityRootCauseClassifier.ClassifierInput input(
            boolean fcbExists,
            @Nullable String fcbFileStatus,
            FacilityStatus novaStatus,
            boolean graceElapsed,
            @Nullable String fcbBusinessError,
            List<OutboxRecordView> forwardRows) {
        return new FacilityRootCauseClassifier.ClassifierInput(
                fcbExists, fcbFileStatus, novaStatus, forwardRows, graceElapsed, fcbBusinessError);
    }

    private static List<OutboxRecordView> processedForward() {
        return List.of(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED));
    }

    private static List<OutboxRecordView> deadLetterForward() {
        return List.of(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.DEAD_LETTER));
    }

    private static OutboxRecordView forward(String eventType, MessageStatus status) {
        return new OutboxRecordView(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "idem-" + eventType,
                UUID.randomUUID(),
                "LoanFacility",
                eventType,
                1,
                1L,
                "trade-loan.facility.events",
                "partition-key",
                status,
                0,
                (Instant) null,
                (String) null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                (Instant) null,
                (Instant) null,
                (String) null);
    }
}
