package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal.IdempotencyState;

import static org.assertj.core.api.Assertions.assertThat;

class RemediationSafetyInvariantsTest {

    private static final UUID UID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000009");

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
        return sweepFor("money", money);
    }

    static Stream<Arguments> nonMoneyStateSweep() {
        return sweepFor("nonMoney", FacilityStatus.APPROVED);
    }

    private static Stream<Arguments> sweepFor(String label, FacilityStatus status) {
        return Stream.of(
                Arguments.of(
                        label + "/absent/completed", input(false, null, status, completed(), false, null, processed())),
                Arguments.of(
                        label + "/absent/noSignal", input(false, null, status, Map.of(), false, null, processed())),
                Arguments.of(
                        label + "/absent/inProgress",
                        input(false, null, status, inProgress(), false, null, processed())),
                Arguments.of(
                        label + "/absent/businessError",
                        input(false, null, status, Map.of(), false, "FCB rejected", processed())),
                Arguments.of(
                        label + "/absent/dltBusiness",
                        input(false, null, status, dltBusiness(), true, null, processed())),
                Arguments.of(
                        label + "/absent/dltTransient",
                        input(false, null, status, dltTransient(), true, null, processed())),
                Arguments.of(label + "/absent/noForward", input(false, null, status, Map.of(), false, null, List.of())),
                Arguments.of(
                        label + "/absent/forwardDeadLetter",
                        input(false, null, status, Map.of(), false, null, deadLetter())),
                Arguments.of(
                        label + "/exists/rankMismatch/completed",
                        input(
                                true,
                                FacilityReconMapping.FCB_REQUEST_LOAN,
                                status,
                                completed(),
                                false,
                                null,
                                processed())),
                Arguments.of(
                        label + "/exists/rankMismatch/noSignal",
                        input(
                                true,
                                FacilityReconMapping.FCB_REQUEST_LOAN,
                                status,
                                Map.of(),
                                false,
                                null,
                                processed())));
    }

    private static FacilityRootCauseClassifier.ClassifierInput input(
            boolean fcbExists,
            @Nullable String fcbFileStatus,
            FacilityStatus novaStatus,
            Map<String, EventPeerSignal> peerSignals,
            boolean dltPresentForFacility,
            @Nullable String fcbBusinessError,
            List<OutboxRecordView> forwardRows) {
        return new FacilityRootCauseClassifier.ClassifierInput(
                fcbExists,
                fcbFileStatus,
                novaStatus,
                forwardRows,
                peerSignals,
                dltPresentForFacility,
                fcbBusinessError);
    }

    private static Map<String, EventPeerSignal> completed() {
        return Map.of(UID.toString(), new EventPeerSignal(UID.toString(), IdempotencyState.COMPLETED, false, null));
    }

    private static Map<String, EventPeerSignal> inProgress() {
        return Map.of(UID.toString(), new EventPeerSignal(UID.toString(), IdempotencyState.IN_PROGRESS, false, null));
    }

    private static Map<String, EventPeerSignal> dltBusiness() {
        return Map.of(UID.toString(), new EventPeerSignal(UID.toString(), IdempotencyState.ABSENT, true, "BUSINESS"));
    }

    private static Map<String, EventPeerSignal> dltTransient() {
        return Map.of(
                UID.toString(),
                new EventPeerSignal(UID.toString(), IdempotencyState.ABSENT, true, "TRANSIENT_INTERNAL"));
    }

    private static List<OutboxRecordView> processed() {
        return List.of(forward(UID, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED));
    }

    private static List<OutboxRecordView> deadLetter() {
        return List.of(forward(UID, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.DEAD_LETTER));
    }

    private static OutboxRecordView forward(UUID eventId, String eventType, MessageStatus status) {
        return new OutboxRecordView(
                UUID.randomUUID(),
                eventId,
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
                LocalDateTime.now(ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC),
                (Instant) null,
                (Instant) null,
                (String) null);
    }
}
