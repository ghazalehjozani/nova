package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.inbox.api.admin.InboxAdminPort;
import ir.dotin.platform.pangaea.outbox.api.MessageStatus;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxAdminPort;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxAdminPort.OutboxRepublishCommand;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordPage;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordView;
import ir.dotin.platform.pangaea.reconciliation.api.admin.RemediateOutcome;
import ir.dotin.platform.pangaea.reconciliation.api.model.Direction;
import ir.dotin.platform.pangaea.reconciliation.api.model.Divergence;
import ir.dotin.platform.pangaea.reconciliation.api.model.OpaqueKey;
import ir.dotin.platform.pangaea.reconciliation.api.model.RemediationKind;
import ir.dotin.platform.pangaea.workflow.api.admin.WorkflowAdminPort;
import ir.dotin.platform.pangaea.workflow.api.admin.WorkflowRunView;
import ir.dotin.platform.pangaea.workflow.api.model.RunId;
import ir.dotin.platform.pangaea.workflow.api.model.RunState;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbOutboxReemitPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbReconStatePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityConvergenceActionRemediateTest {

    private static final Instant NOW = Instant.parse("2026-06-05T12:00:00Z");
    private static final UUID FACILITY = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CORRELATION = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID EVENT_ID_A = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID EVENT_ID_B = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    private static final UUID ROW_ID_A = UUID.fromString("cccccccc-0000-0000-0000-000000000003");
    private static final UUID ROW_ID_B = UUID.fromString("dddddddd-0000-0000-0000-000000000004");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private OutboxAdminPort outboxAdminPort;

    @Mock
    private InboxAdminPort inboxAdminPort;

    @Mock
    private WorkflowAdminPort workflowAdminPort;

    @Mock
    private FcbReconStatePort fcbReconStatePort;

    @Mock
    private FcbOutboxReemitPort fcbOutboxReemitPort;

    @Mock
    private FacilityReconReadPort readPort;

    private ReconciliationSourceProperties properties;
    private FacilityConvergenceAction action;

    @BeforeEach
    void setUp() {
        properties = new ReconciliationSourceProperties();
        properties.setGraceWindow(Duration.ofMinutes(15));
        properties.setReplayForwardEnabled(true);
        action = new FacilityConvergenceAction(
                outboxAdminPort,
                inboxAdminPort,
                workflowAdminPort,
                fcbReconStatePort,
                fcbOutboxReemitPort,
                readPort,
                properties,
                clock);
    }

    @Test
    void flagOffReturnsNotEnabledAndNeverRepublishes() {
        properties.setReplayForwardEnabled(false);

        RemediateOutcome outcome = action.remediate(
                key(),
                divergence(),
                RemediationKind.REPLAY_FORWARD,
                "any-hash",
                "operator-reason",
                CORRELATION.toString());

        assertThat(outcome).isInstanceOf(RemediateOutcome.NotEnabled.class);
        assertThat(((RemediateOutcome.NotEnabled) outcome).reason()).isEqualTo("replay-forward-disabled");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void wrongKindRejectedAndNeverRepublishes() {
        RemediateOutcome outcome = action.remediate(
                key(),
                divergence(),
                RemediationKind.REVERSE_NOVA,
                "any-hash",
                "operator-reason",
                CORRELATION.toString());

        assertThat(outcome).isInstanceOf(RemediateOutcome.Rejected.class);
        assertThat(((RemediateOutcome.Rejected) outcome).reason()).startsWith("unsupported-kind:");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void happyPathRepublishesExactlyTheEventIdsAndIsIdempotent() {
        long modifiedAt = NOW.minus(Duration.ofMinutes(30)).toEpochMilli();
        List<OutboxRecordView> rows = List.of(
                forward(ROW_ID_A, EVENT_ID_A, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L),
                forward(
                        ROW_ID_B,
                        EVENT_ID_B,
                        "TRADE_LOAN_FACILITY_IRREGULAR_TRANCHE_DISBURSED",
                        MessageStatus.PROCESSED,
                        11L));
        stubNova(FacilityStatus.FULLY_DISBURSED, modifiedAt);
        stubOutbox(rows);
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(FACILITY.toString())).thenReturn(Result.success(fcbAbsentReachable()));
        when(outboxAdminPort.republish(any())).thenReturn(2L);

        String hash = freshHashFor(rows, FacilityStatus.FULLY_DISBURSED, fcbAbsentReachable(), true);

        RemediateOutcome first = action.remediate(
                key(), divergence(), RemediationKind.REPLAY_FORWARD, hash, "operator-reason", CORRELATION.toString());

        assertThat(first).isInstanceOf(RemediateOutcome.Accepted.class);
        assertThat(((RemediateOutcome.Accepted) first).detail()).isEqualTo("re-driven:2");

        ArgumentCaptor<OutboxRepublishCommand> captor = ArgumentCaptor.forClass(OutboxRepublishCommand.class);
        verify(outboxAdminPort, times(1)).republish(captor.capture());
        List<UUID> firstIds = captor.getValue().eventIds();
        assertThat(firstIds).containsExactly(EVENT_ID_A, EVENT_ID_B);
        assertThat(firstIds).doesNotContain(ROW_ID_A, ROW_ID_B);

        RemediateOutcome second = action.remediate(
                key(), divergence(), RemediationKind.REPLAY_FORWARD, hash, "operator-reason", CORRELATION.toString());

        assertThat(second).isInstanceOf(RemediateOutcome.Accepted.class);
        ArgumentCaptor<OutboxRepublishCommand> captor2 = ArgumentCaptor.forClass(OutboxRepublishCommand.class);
        verify(outboxAdminPort, times(2)).republish(captor2.capture());
        assertThat(captor2.getAllValues().get(0).eventIds())
                .isEqualTo(captor2.getAllValues().get(1).eventIds());
    }

    @Test
    void staleDossierHashIsRejectedWithoutRepublishing() {
        long modifiedAt = NOW.minus(Duration.ofMinutes(30)).toEpochMilli();
        List<OutboxRecordView> rows = List.of(
                forward(ROW_ID_A, EVENT_ID_A, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L));
        stubNova(FacilityStatus.FULLY_DISBURSED, modifiedAt);
        stubOutbox(rows);
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(FACILITY.toString())).thenReturn(Result.success(fcbAbsentReachable()));

        RemediateOutcome outcome = action.remediate(
                key(),
                divergence(),
                RemediationKind.REPLAY_FORWARD,
                "this-hash-does-not-match-fresh-state",
                "operator-reason",
                CORRELATION.toString());

        assertThat(outcome).isInstanceOf(RemediateOutcome.DossierStale.class);
        assertThat(((RemediateOutcome.DossierStale) outcome).reason()).isEqualTo("state-moved");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void reclassifiedToPartialApplyIsRejectedNotReplayable() {
        long modifiedAt = NOW.minus(Duration.ofMinutes(30)).toEpochMilli();
        List<OutboxRecordView> rows = List.of(
                forward(ROW_ID_A, EVENT_ID_A, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L));
        ReconLoanFileState fcb = fcbPresent("REQUEST_LOAN");
        stubNova(FacilityStatus.FULLY_DISBURSED, modifiedAt);
        stubOutbox(rows);
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(FACILITY.toString())).thenReturn(Result.success(fcb));

        String hash = freshHashFor(rows, FacilityStatus.FULLY_DISBURSED, fcb, false);

        RemediateOutcome outcome = action.remediate(
                key(), divergence(), RemediationKind.REPLAY_FORWARD, hash, "operator-reason", CORRELATION.toString());

        assertThat(outcome).isInstanceOf(RemediateOutcome.Rejected.class);
        assertThat(((RemediateOutcome.Rejected) outcome).reason()).isEqualTo("not-replayable");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void fcbUnreachableInsideLockIsRejectedWithoutMutation() {
        long modifiedAt = NOW.minus(Duration.ofMinutes(30)).toEpochMilli();
        stubNova(FacilityStatus.FULLY_DISBURSED, modifiedAt);
        stubOutbox(List.of(
                forward(ROW_ID_A, EVENT_ID_A, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L)));
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(FACILITY.toString())).thenReturn(Result.success(fcbUnreachable()));

        RemediateOutcome outcome = action.remediate(
                key(),
                divergence(),
                RemediationKind.REPLAY_FORWARD,
                "any-hash",
                "operator-reason",
                CORRELATION.toString());

        assertThat(outcome).isInstanceOf(RemediateOutcome.Rejected.class);
        assertThat(((RemediateOutcome.Rejected) outcome).reason()).isEqualTo("fcb-unreachable");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void sagaInFlightIsRejectedWithoutRepublishing() {
        stubOutbox(List.of(
                forward(ROW_ID_A, EVENT_ID_A, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L)));
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of(runInState(RunState.EXECUTING)));

        RemediateOutcome outcome = action.remediate(
                key(),
                divergence(),
                RemediationKind.REPLAY_FORWARD,
                "any-hash",
                "operator-reason",
                CORRELATION.toString());

        assertThat(outcome).isInstanceOf(RemediateOutcome.Rejected.class);
        assertThat(((RemediateOutcome.Rejected) outcome).reason()).isEqualTo("saga-in-flight");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void sagaLockedIsRejectedWithoutRepublishing() {
        stubOutbox(List.of(
                forward(ROW_ID_A, EVENT_ID_A, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L)));
        when(workflowAdminPort.runUnderCorrelationGuard(anyString(), any())).thenReturn(Optional.empty());

        RemediateOutcome outcome = action.remediate(
                key(),
                divergence(),
                RemediationKind.REPLAY_FORWARD,
                "any-hash",
                "operator-reason",
                CORRELATION.toString());

        assertThat(outcome).isInstanceOf(RemediateOutcome.Rejected.class);
        assertThat(((RemediateOutcome.Rejected) outcome).reason()).isEqualTo("saga-locked");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void novaMissingInsideLockIsRejected() {
        stubOutbox(List.of(
                forward(ROW_ID_A, EVENT_ID_A, "TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L)));
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(readPort.findById(FACILITY.toString())).thenReturn(Optional.empty());

        RemediateOutcome outcome = action.remediate(
                key(),
                divergence(),
                RemediationKind.REPLAY_FORWARD,
                "any-hash",
                "operator-reason",
                CORRELATION.toString());

        assertThat(outcome).isInstanceOf(RemediateOutcome.Rejected.class);
        assertThat(((RemediateOutcome.Rejected) outcome).reason()).isEqualTo("nova-missing");
        verify(outboxAdminPort, never()).republish(any());
    }

    private String freshHashFor(
            List<OutboxRecordView> rows, FacilityStatus novaStatus, ReconLoanFileState fcb, boolean graceElapsed) {
        List<OutboxRecordView> forwardRows = new ArrayList<>(rows.stream()
                .filter(r -> FacilityReconMapping.fcbRankForEvent(r.eventType()) >= 0)
                .toList());
        FacilityRootCauseClassifier classifier = new FacilityRootCauseClassifier();
        FacilityClassification c = classifier.classify(new FacilityRootCauseClassifier.ClassifierInput(
                fcb.exists(), fcb.fileStatus(), novaStatus, forwardRows, graceElapsed, null));
        return FacilityDossierBuilder.computeHash(
                c.rootCause(), fcb, novaStatus, forwardRows, graceElapsed, c.recommendedKind());
    }

    private void stubNova(FacilityStatus status, long modifiedAtEpochMs) {
        when(readPort.findById(FACILITY.toString()))
                .thenReturn(Optional.of(new FacilityReconRow(FACILITY.toString(), status, modifiedAtEpochMs)));
    }

    private void stubOutbox(List<OutboxRecordView> rows) {
        when(outboxAdminPort.search(any())).thenReturn(new OutboxRecordPage(rows, null, false, rows.size()));
    }

    private void stubGuardRunsSupplier() {
        lenient()
                .when(workflowAdminPort.runUnderCorrelationGuard(anyString(), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return Optional.ofNullable(supplier.get());
                });
    }

    private static WorkflowRunView runInState(RunState state) {
        return new WorkflowRunView(
                RunId.of(UUID.randomUUID()),
                "TradeLoanWorkflow",
                state,
                CORRELATION.toString(),
                null,
                NOW,
                NOW,
                null,
                0L,
                null);
    }

    private static OpaqueKey key() {
        return new OpaqueKey(FACILITY.toString());
    }

    private static Divergence divergence() {
        return Divergence.lagging(Direction.SOURCE_AHEAD, "test");
    }

    private static ReconLoanFileState fcbAbsentReachable() {
        return new ReconLoanFileState(false, null, "manual", NOW.toEpochMilli(), true, null);
    }

    private static ReconLoanFileState fcbUnreachable() {
        return new ReconLoanFileState(false, null, "manual", NOW.toEpochMilli(), false, null);
    }

    private static ReconLoanFileState fcbPresent(String fileStatus) {
        return new ReconLoanFileState(true, fileStatus, "manual", NOW.toEpochMilli(), true, "outbox-1");
    }

    private static OutboxRecordView forward(
            UUID rowId, UUID eventId, String eventType, MessageStatus status, long sequence) {
        return new OutboxRecordView(
                rowId,
                eventId,
                "idem-" + eventType,
                FACILITY,
                "TradeLoanFacility",
                eventType,
                1,
                sequence,
                "trade-loan.facility.events",
                "partition-key",
                status,
                0,
                (Instant) null,
                (String) null,
                CORRELATION,
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                (Instant) null,
                (Instant) null,
                (String) null);
    }
}
