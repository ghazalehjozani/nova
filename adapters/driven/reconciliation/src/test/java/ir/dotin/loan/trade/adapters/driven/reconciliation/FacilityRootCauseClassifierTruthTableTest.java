package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
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
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal.IdempotencyState;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Signal-based truth table (LN-59513): the apply-lost vs lag verdict turns on FCB's per-uid peer signal (idempotency +
 * dead-letter), never on elapsed time. There is no {@code Clock} / time input anywhere in {@link ClassifierInput}.
 */
class FacilityRootCauseClassifierTruthTableTest {

    private static final UUID UID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");

    private final FacilityRootCauseClassifier classifier = new FacilityRootCauseClassifier();

    // Regression guard (ADR-0008): rows through "absent+forwardNotProcessed=lag" predate the classifier's terminal-
    // category allowlist and must keep their exact RootCause/Confidence/SafetyTier/RemediationKind — the allowlist
    // only widens which DLT categories are re-drivable, it must never change an already-decided row.
    static Stream<Arguments> truthTable() {
        return Stream.of(
                Arguments.of(
                        "exists+rankMismatch+noSignal",
                        input(
                                true,
                                FacilityReconMapping.FCB_REQUEST_LOAN,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                Map.of(),
                                false,
                                null),
                        RootCause.PARTIAL_APPLY,
                        Confidence.MEDIUM,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.MANUAL_DATA_FIX),
                Arguments.of(
                        "absent+idempotencyAbsent+noSignal",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                Map.of(),
                                false,
                                null),
                        RootCause.FCB_LAG,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.NONE),
                Arguments.of(
                        "absent+inProgress",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.IN_PROGRESS, false, null)),
                                false,
                                null),
                        RootCause.FCB_LAG,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.NONE),
                Arguments.of(
                        "absent+businessError",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                Map.of(),
                                false,
                                "FCB rejected: blacklisted"),
                        RootCause.FCB_BUSINESS_REJECT,
                        Confidence.HIGH,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.MANUAL_DATA_FIX),
                Arguments.of(
                        "absent+dltBusiness=terminalReject",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "BUSINESS")),
                                true,
                                null),
                        RootCause.FCB_BUSINESS_REJECT,
                        Confidence.HIGH,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.MANUAL_DATA_FIX),
                Arguments.of(
                        "absent+completed+money=applyLost",
                        input(
                                false,
                                null,
                                FacilityStatus.FULLY_DISBURSED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.COMPLETED, false, null)),
                                false,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.MONEY_GATED,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+completed+nonMoney=applyLost",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.COMPLETED, false, null)),
                                false,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+dltTransientExhausted=applyLost",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "TRANSIENT_INTERNAL")),
                                true,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.REPLAY_FORWARD),
                // ADR-0008 truth table: one case per NovaDltCategory value, plus null and an unrecognised string.
                // TRANSIENT_INTERNAL is covered by "absent+dltTransientExhausted=applyLost" above, BUSINESS by
                // "absent+dltBusiness=terminalReject" above; TRANSIENT_EXTERNAL/PERMANENT_HTTP/PERMANENT_CONFIG/
                // POISON/UNKNOWN/null/unrecognised follow here.
                Arguments.of(
                        "absent+dltTransientExternal=applyLost",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "TRANSIENT_EXTERNAL")),
                                true,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+dltUnknown=applyLost",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "UNKNOWN")),
                                true,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+dltNullCategory=applyLost",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, null)),
                                true,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+dltUnrecognizedCategory=applyLost",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "SOME_NEW_CATEGORY")),
                                true,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+dltUnknown+money=applyLostMoneyGated",
                        input(
                                false,
                                null,
                                FacilityStatus.FULLY_DISBURSED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "UNKNOWN")),
                                true,
                                null),
                        RootCause.FCB_APPLY_LOST,
                        Confidence.HIGH,
                        SafetyTier.MONEY_GATED,
                        RemediationKind.REPLAY_FORWARD),
                Arguments.of(
                        "absent+dltPermanentHttp=terminalReject",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "PERMANENT_HTTP")),
                                true,
                                null),
                        RootCause.FCB_BUSINESS_REJECT,
                        Confidence.HIGH,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.MANUAL_DATA_FIX),
                Arguments.of(
                        "absent+dltPermanentConfig=terminalReject",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "PERMANENT_CONFIG")),
                                true,
                                null),
                        RootCause.FCB_BUSINESS_REJECT,
                        Confidence.HIGH,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.MANUAL_DATA_FIX),
                Arguments.of(
                        "absent+dltPoison=terminalReject",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.PROCESSED)),
                                signals(sig(UID, IdempotencyState.ABSENT, true, "POISON")),
                                true,
                                null),
                        RootCause.FCB_BUSINESS_REJECT,
                        Confidence.HIGH,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.MANUAL_DATA_FIX),
                Arguments.of(
                        "absent+noForwardOutbox",
                        input(false, null, FacilityStatus.APPROVED, List.of(), Map.of(), false, null),
                        RootCause.NOVA_PHANTOM,
                        Confidence.HIGH,
                        SafetyTier.MANUAL_ONLY,
                        RemediationKind.REVERSE_NOVA),
                Arguments.of(
                        "absent+forwardNotProcessed=lag",
                        input(
                                false,
                                null,
                                FacilityStatus.APPROVED,
                                List.of(forward(UID, "TRADE_LOAN_FACILITY_APPROVED", MessageStatus.DEAD_LETTER)),
                                Map.of(),
                                false,
                                null),
                        RootCause.FCB_LAG,
                        Confidence.HIGH,
                        SafetyTier.AUTO_SAFE,
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
        FacilityClassification result = classifier.classify(input(
                false,
                null,
                FacilityStatus.FULLY_DISBURSED,
                List.of(forward(UID, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED)),
                signals(sig(UID, IdempotencyState.COMPLETED, false, null)),
                false,
                null));

        assertThat(result.evidence()).isNotEmpty();
    }

    private static FacilityRootCauseClassifier.ClassifierInput input(
            boolean fcbExists,
            @Nullable String fcbFileStatus,
            FacilityStatus novaStatus,
            List<OutboxRecordView> forwardRows,
            Map<String, EventPeerSignal> peerSignals,
            boolean dltPresentForFacility,
            @Nullable String fcbBusinessError) {
        return new FacilityRootCauseClassifier.ClassifierInput(
                fcbExists,
                fcbFileStatus,
                novaStatus,
                forwardRows,
                peerSignals,
                dltPresentForFacility,
                fcbBusinessError);
    }

    private static Map<String, EventPeerSignal> signals(EventPeerSignal... sigs) {
        return Stream.of(sigs).collect(java.util.stream.Collectors.toMap(EventPeerSignal::eventUid, s -> s));
    }

    private static EventPeerSignal sig(
            UUID eventId, IdempotencyState state, boolean dltDead, @Nullable String dltCategory) {
        return new EventPeerSignal(eventId.toString(), state, dltDead, dltCategory);
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
