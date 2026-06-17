package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.compensation.step;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateIrregularDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RevertIrregularDisbursementStepTest {

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private Clock clock;

    @Mock
    private TradeLoanFacility facility;

    @Mock
    private TrackedTransactionNumber tracked;

    @InjectMocks
    private RevertIrregularDisbursementStep step;

    @Test
    void executeFailsWhenFacilityMissing() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(facilityRepository.findById(LoanFacilityId.of(facilityId))).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verify(facilityRepository, never()).save(facility);
    }

    @Test
    void executeRevertsDisbursementAndSavesFacilityWhenScheduleAbsent() {
        UUID facilityId = UUID.randomUUID();
        RevertIrregularDisbursementData data = new RevertIrregularDisbursementData(
                CompensateIrregularDisbursementCommand.of(UUID.randomUUID(), 0L, facilityId, null, null),
                new ReversalPreparation(new AtomicReference<>()));
        WorkflowContextStub ctx = new WorkflowContextStub(data);

        when(facilityRepository.findById(LoanFacilityId.of(facilityId))).thenReturn(Optional.of(facility));
        when(facility.revertIrregularTrancheDisbursement(clock)).thenReturn(Result.success(Optional.of(tracked)));
        when(facility.domainEvents()).thenReturn(List.of());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facilityRepository).save(facility);
        assertThat(data.prepared().reversals().get()).isEqualTo(tracked);
    }

    private static RevertIrregularDisbursementData data(UUID facilityId) {
        return new RevertIrregularDisbursementData(
                CompensateIrregularDisbursementCommand.of(UUID.randomUUID(), 0L, facilityId, null, null),
                new ReversalPreparation(new AtomicReference<>()));
    }
}
