package ir.dotin.loan.trade.adapters.driven.reconciliation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import ir.dotin.platform.pangaea.reconciliation.api.model.AutonomyTier;
import ir.dotin.platform.pangaea.reconciliation.api.model.ConvergeOutcome;
import ir.dotin.platform.pangaea.reconciliation.api.model.Direction;
import ir.dotin.platform.pangaea.reconciliation.api.model.Divergence;
import ir.dotin.platform.pangaea.reconciliation.api.model.OpaqueKey;
import ir.dotin.platform.pangaea.workflow.api.admin.WorkflowAdminPort;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbOutboxReemitPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbReconStatePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Guarantor-drift convergence (LN-59442 M1): the action re-drives the latest PROCESSED guarantors-changed outbox row
 * (never a minted event), escalates to an operator when there is no such row, defers on an in-flight row, and is fully
 * gated by the dark-launch flag.
 */
@ExtendWith(MockitoExtension.class)
class FacilityConvergenceActionGuarantorDriftTest {

    private static final UUID FACILITY = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CORRELATION = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID GUARANTORS_EVENT_OLD = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID GUARANTORS_EVENT_NEW = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    // 31 minutes ago in epoch millis, so the recency defer (30 min) never fires.
    private static final long MODIFIED_AT = System.currentTimeMillis() - 31L * 60L * 1000L;

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
        properties.setGuarantorDriftEnabled(true);
        action = new FacilityConvergenceAction(
                outboxAdminPort,
                inboxAdminPort,
                workflowAdminPort,
                fcbReconStatePort,
                fcbOutboxReemitPort,
                readPort,
                properties);
        stubNova(FacilityStatus.APPROVED);
        stubGuardRunsSupplier();
    }

    @Test
    void republishesLatestProcessedGuarantorsChangedRow() {
        stubOutbox(List.of(
                guarantorEvent(
                        GUARANTORS_EVENT_OLD, "TRADE_LOAN_FACILITY_GUARANTOR_ADDED", MessageStatus.PROCESSED, 10L),
                guarantorEvent(
                        GUARANTORS_EVENT_NEW, "TRADE_LOAN_FACILITY_GUARANTOR_REMOVED", MessageStatus.PROCESSED, 20L)));
        when(outboxAdminPort.republish(any())).thenReturn(1L);

        ConvergeOutcome outcome = action.converge(key(), driftDivergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.RetryLater.class);
        ArgumentCaptor<OutboxRepublishCommand> captor = ArgumentCaptor.forClass(OutboxRepublishCommand.class);
        verify(outboxAdminPort).republish(captor.capture());
        // The LATEST (highest sequence) guarantors-changed row is the one re-driven.
        assertThat(captor.getValue().eventIds()).containsExactly(GUARANTORS_EVENT_NEW);
    }

    @Test
    void noProcessedRowEscalatesToOperator() {
        // Only a dead-lettered guarantors-changed row exists → nothing to re-drive → needs operator.
        stubOutbox(List.of(guarantorEvent(
                GUARANTORS_EVENT_OLD, "TRADE_LOAN_FACILITY_GUARANTOR_ADDED", MessageStatus.DEAD_LETTER, 10L)));
        // The escalation builds a dossier by re-reading FCB state — answer reachably so the dossier shows both sides.
        when(fcbReconStatePort.loadReconState(eq(FACILITY.toString()), isNull()))
                .thenReturn(Result.success(
                        new ReconLoanFileState(true, "APPROVE_LOAN", "manual", 1L, true, null, List.of(), false)));

        ConvergeOutcome outcome = action.converge(key(), driftDivergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.NeedsOperator.class);
        assertThat(((ConvergeOutcome.NeedsOperator) outcome).reason()).isEqualTo("UNKNOWN");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void inFlightGuarantorsChangedRowDefers() {
        stubOutbox(List.of(guarantorEvent(
                GUARANTORS_EVENT_NEW, "TRADE_LOAN_FACILITY_GUARANTOR_REMOVED", MessageStatus.PENDING, 20L)));

        ConvergeOutcome outcome = action.converge(key(), driftDivergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.RetryLater.class);
        assertThat(((ConvergeOutcome.RetryLater) outcome).reason()).isEqualTo("guarantors-changed-in-flight");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void flagOffIsNotApplicableAndNeverRepublishes() {
        properties.setGuarantorDriftEnabled(false);
        stubOutbox(List.of(guarantorEvent(
                GUARANTORS_EVENT_NEW, "TRADE_LOAN_FACILITY_GUARANTOR_ADDED", MessageStatus.PROCESSED, 20L)));

        ConvergeOutcome outcome = action.converge(key(), driftDivergence(), CORRELATION.toString(), false);

        assertThat(outcome).isInstanceOf(ConvergeOutcome.NotApplicable.class);
        assertThat(((ConvergeOutcome.NotApplicable) outcome).reason()).isEqualTo("guarantor-drift-disabled");
        verify(outboxAdminPort, never()).republish(any());
    }

    @Test
    void tierIsAutoSafeForNonTerminalGuarantorDrift() {
        // Guarantor change is not money-moving — a non-terminal facility's guarantor-drift is auto-drivable.
        assertThat(action.tierFor(key(), driftDivergence())).isEqualTo(AutonomyTier.AUTO_SAFE);
    }

    @Test
    void tierIsOperatorGatedForTerminalFacility() {
        stubNova(FacilityStatus.CLOSED_PAID_OFF);
        assertThat(action.tierFor(key(), driftDivergence())).isEqualTo(AutonomyTier.OPERATOR_GATED);
    }

    // ── helpers ──

    private void stubNova(FacilityStatus status) {
        lenient()
                .when(readPort.findById(FACILITY.toString()))
                .thenReturn(Optional.of(new FacilityReconRow(FACILITY.toString(), status, MODIFIED_AT, "1-1-1-1")));
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
        lenient().when(workflowAdminPort.findByCorrelation(anyString())).thenReturn(List.of());
    }

    private static OpaqueKey key() {
        return new OpaqueKey(FACILITY.toString());
    }

    private static Divergence driftDivergence() {
        return Divergence.lagging(Direction.SOURCE_AHEAD, "guarantor-drift");
    }

    private static OutboxRecordView guarantorEvent(
            UUID eventId, String eventType, MessageStatus status, long sequence) {
        return new OutboxRecordView(
                UUID.randomUUID(),
                eventId,
                "idem-" + eventId,
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
