package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.inbox.api.admin.InboxAdminPort;
import ir.dotin.platform.pangaea.outbox.api.MessageStatus;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxAdminPort;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordPage;
import ir.dotin.platform.pangaea.outbox.api.admin.OutboxRecordView;
import ir.dotin.platform.pangaea.reconciliation.api.model.ConvergeOutcome;
import ir.dotin.platform.pangaea.reconciliation.api.model.Direction;
import ir.dotin.platform.pangaea.reconciliation.api.model.Divergence;
import ir.dotin.platform.pangaea.reconciliation.api.model.OpaqueKey;
import ir.dotin.platform.pangaea.reconciliation.api.model.OperatorDossier;
import ir.dotin.platform.pangaea.reconciliation.api.model.RemediationKind;
import ir.dotin.platform.pangaea.reconciliation.api.model.RootCause;
import ir.dotin.platform.pangaea.reconciliation.api.model.SafetyTier;
import ir.dotin.platform.pangaea.workflow.api.admin.WorkflowAdminPort;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal.IdempotencyState;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbOutboxReemitPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbReconStatePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityConvergenceActionDossierTest {

    private static final UUID FACILITY = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CORRELATION = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID EVENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final long MODIFIED_AT = 1_000_000L;

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
        action = new FacilityConvergenceAction(
                outboxAdminPort,
                inboxAdminPort,
                workflowAdminPort,
                fcbReconStatePort,
                fcbOutboxReemitPort,
                readPort,
                properties);
    }

    @Test
    void moneyStateApplyLostBySignalReturnsReplayForwardDossier() {
        stubNova(FacilityStatus.FULLY_DISBURSED);
        stubOutbox(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L));
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(eq(FACILITY.toString()), any()))
                .thenReturn(Result.success(fcbAbsentWithCompleted()));

        ConvergeOutcome outcome = action.converge(key(), divergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.NeedsOperator.class);
        OperatorDossier dossier = Objects.requireNonNull(((ConvergeOutcome.NeedsOperator) outcome).dossier());
        assertThat(dossier.rootCause()).isEqualTo(RootCause.FCB_APPLY_LOST);
        assertThat(dossier.recommended().safetyTier()).isEqualTo(SafetyTier.MONEY_GATED);
        assertThat(dossier.recommended().kind()).isEqualTo(RemediationKind.REPLAY_FORWARD);
        assertThat(dossier.dossierHash()).isNotBlank();
    }

    @Test
    void moneyStateNoSignalReturnsFcbLagRetry() {
        stubNova(FacilityStatus.FULLY_DISBURSED);
        stubOutbox(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L));
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(eq(FACILITY.toString()), any()))
                .thenReturn(Result.success(fcbAbsentReachable()));

        ConvergeOutcome outcome = action.converge(key(), divergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.RetryLater.class);
        assertThat(((ConvergeOutcome.RetryLater) outcome).reason()).isEqualTo("fcb-lag");
    }

    @Test
    void moneyStateFcbUnreachableRetriesAndDoesNotClassify() {
        stubNova(FacilityStatus.FULLY_DISBURSED);
        stubOutbox(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L));
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(eq(FACILITY.toString()), any()))
                .thenReturn(Result.success(fcbUnreachable()));

        ConvergeOutcome outcome = action.converge(key(), divergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.RetryLater.class);
        assertThat(((ConvergeOutcome.RetryLater) outcome).reason()).isEqualTo("fcb-unreachable");
    }

    @Test
    void terminalDivergentReturnsDossierNotBareString() {
        stubNova(FacilityStatus.CLOSED_PAID_OFF);
        stubOutbox(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L));
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(eq(FACILITY.toString()), any()))
                .thenReturn(Result.success(fcbPresent("GIVE_LOAN")));

        ConvergeOutcome outcome = action.converge(key(), divergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.NeedsOperator.class);
        OperatorDossier dossier = Objects.requireNonNull(((ConvergeOutcome.NeedsOperator) outcome).dossier());
        assertThat(dossier.dossierHash()).isNotBlank();
        verify(fcbReconStatePort).loadReconState(eq(FACILITY.toString()), any());
    }

    @Test
    void fcbUnreachableTerminalNeverClassifies() {
        stubNova(FacilityStatus.CLOSED_PAID_OFF);
        stubOutbox(forward("TRADE_LOAN_FACILITY_FULLY_DISBURSED", MessageStatus.PROCESSED, 10L));
        stubGuardRunsSupplier();
        when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
        when(fcbReconStatePort.loadReconState(eq(FACILITY.toString()), any()))
                .thenReturn(Result.success(fcbUnreachable()));

        ConvergeOutcome outcome = action.converge(key(), divergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.RetryLater.class);
        assertThat(((ConvergeOutcome.RetryLater) outcome).reason()).isEqualTo("fcb-unreachable");
        verify(outboxAdminPort, never()).republish(any());
    }

    private void stubNova(FacilityStatus status) {
        when(readPort.findById(FACILITY.toString()))
                .thenReturn(Optional.of(
                        new FacilityReconRow(FACILITY.toString(), status, MODIFIED_AT, "1-1404-10088-279")));
    }

    private void stubOutbox(OutboxRecordView row) {
        when(outboxAdminPort.search(any())).thenReturn(new OutboxRecordPage(List.of(row), null, false, 1));
    }

    private void stubGuardRunsSupplier() {
        lenient()
                .when(workflowAdminPort.runUnderCorrelationGuard(anyString(), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return Optional.ofNullable(supplier.get());
                });
    }

    private static OpaqueKey key() {
        return new OpaqueKey(FACILITY.toString());
    }

    private static Divergence divergence() {
        return Divergence.lagging(Direction.SOURCE_AHEAD, "test");
    }

    private static ReconLoanFileState fcbAbsentReachable() {
        return new ReconLoanFileState(false, null, "manual", MODIFIED_AT, true, null, List.of(), false);
    }

    private static ReconLoanFileState fcbAbsentWithCompleted() {
        return new ReconLoanFileState(
                false,
                null,
                "manual",
                MODIFIED_AT,
                true,
                null,
                List.of(new EventPeerSignal(EVENT_ID.toString(), IdempotencyState.COMPLETED, false, null)),
                false);
    }

    private static ReconLoanFileState fcbUnreachable() {
        return new ReconLoanFileState(false, null, "manual", MODIFIED_AT, false, null, List.of(), false);
    }

    private static ReconLoanFileState fcbPresent(String fileStatus) {
        return new ReconLoanFileState(true, fileStatus, "manual", MODIFIED_AT, true, "outbox-1", List.of(), false);
    }

    private static OutboxRecordView forward(String eventType, MessageStatus status, long sequence) {
        return new OutboxRecordView(
                UUID.randomUUID(),
                EVENT_ID,
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
                LocalDateTime.now(ZoneOffset.UTC),
                LocalDateTime.now(ZoneOffset.UTC),
                (Instant) null,
                (Instant) null,
                (String) null);
    }
}
