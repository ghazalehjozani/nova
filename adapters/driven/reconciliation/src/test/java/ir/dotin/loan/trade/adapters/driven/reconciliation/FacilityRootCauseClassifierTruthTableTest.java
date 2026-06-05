package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
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

class FacilityRootCauseClassifierTruthTableTest {

    private final FacilityRootCauseClassifier classifier = new FacilityRootCauseClassifier();

    static Stream<Arguments> truthTable() {
        return Stream.of(
                Arguments.of(
                        "exists+rankMismatch",
                        new FacilityRootCauseClassifier.ClassifierInput(
                                true,
                                FacilityReconMapping.FCB_REQUEST_LOAN,
                                FacilityStatus.APPROVED,
                                List.of(forward("TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                true,
                                null),
                        RootCause.PARTIAL_APPLY,
                        Confidence.MEDIUM,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.MANUAL_DATA_FIX),
                Arguments.of(
                        "absent+notGrace",
                        new FacilityRootCauseClassifier.ClassifierInput(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward("TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                false,
                                null),
                        RootCause.FCB_LAG,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.NONE),
                Arguments.of(
                        "absent+grace+businessError",
                        new FacilityRootCauseClassifier.ClassifierInput(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward("TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                true,
                                "FCB rejected: blacklisted"),
                        RootCause.FCB_BUSINESS_REJECT,
                        Confidence.HIGH,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.MANUAL_DATA_FIX),
                Arguments.of(
                        "absent+grace+processed+money",
                        new FacilityRootCauseClassifier.ClassifierInput(
                                false,
                                null,
                                FacilityStatus.FULLY_DISBURSED,
                                List.of(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED)),
                                true,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.MONEY_GATED,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+grace+processed+nonMoney",
                        new FacilityRootCauseClassifier.ClassifierInput(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward("TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                true,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+grace+noForwardOutbox",
                        new FacilityRootCauseClassifier.ClassifierInput(
                                false, null, FacilityStatus.APPROVED, List.of(), true, null),
                        RootCause.NOVA_PHANTOM,
                        Confidence.HIGH,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.REVERSE_NOVA),
                Arguments.of(
                        "absent+grace+forwardNotProcessed",
                        new FacilityRootCauseClassifier.ClassifierInput(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward("TRADE_LOAN_FACILITY_APPROVED", MessageStatus.DEAD_LETTER)),
                                true,
                                null),
                        RootCause.UNKNOWN,
                        Confidence.LOW,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.NONE));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("truthTable")
    void classifiesEachBranch(
            String name,
            FacilityRootCauseClassifier.ClassifierInput input,
            RootCause expectedRootCause,
            Confidence expectedConfidence,
            SafetyTier expectedSafetyTier,
            RemediationKind expectedKind) {
        FacilityClassification result = classifier.classify(input);

        assertThat(result.rootCause()).isEqualTo(expectedRootCause);
        assertThat(result.confidence()).isEqualTo(expectedConfidence);
        assertThat(result.safetyTier()).isEqualTo(expectedSafetyTier);
        assertThat(result.recommendedKind()).isEqualTo(expectedKind);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("truthTable")
    void nonHighConfidenceIsAlwaysManualOnly(
            String name,
            FacilityRootCauseClassifier.ClassifierInput input,
            RootCause expectedRootCause,
            Confidence expectedConfidence,
            SafetyTier expectedSafetyTier,
            RemediationKind expectedKind) {
        FacilityClassification result = classifier.classify(input);

        if (result.confidence() != Confidence.HIGH) {
            assertThat(result.safetyTier()).isEqualTo(SafetyTier.MANUAL_ONLY);
        }
    }

    @Test
    void emitsEvidence() {
        FacilityClassification result = classifier.classify(new FacilityRootCauseClassifier.ClassifierInput(
                false,
                null,
                FacilityStatus.FULLY_DISBURSED,
                List.of(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED)),
                true,
                null));

        assertThat(result.evidence()).isNotEmpty();
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
