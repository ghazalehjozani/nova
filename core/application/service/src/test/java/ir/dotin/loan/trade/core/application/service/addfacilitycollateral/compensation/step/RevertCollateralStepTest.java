package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.step;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RevertCollateralStepTest {

    @Mock
    private TradeLoanFacilityRepository repository;

    @Mock
    private Clock clock;

    @Mock
    private TradeLoanFacility facility;

    @InjectMocks
    private RevertCollateralStep step;

    @Test
    void executeIsNoOpWhenPreparedNoOp() {
        RevertCollateralData data = new RevertCollateralData(command(), new ReleasePreparation(true));
        WorkflowContextStub ctx = new WorkflowContextStub(data);

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(repository);
    }

    @Test
    void executeFailsWhenFacilityMissing() {
        CompensateCollateralCommand command = command();
        RevertCollateralData data = new RevertCollateralData(command, new ReleasePreparation(false));
        WorkflowContextStub ctx = new WorkflowContextStub(data);

        when(repository.findById(LoanFacilityId.of(command.loanFacilityId()))).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verify(repository, never()).save(facility);
    }

    @Test
    void executeRevertsCollateralsAndSavesFacility() {
        CompensateCollateralCommand command = command();
        RevertCollateralData data = new RevertCollateralData(command, new ReleasePreparation(false));
        WorkflowContextStub ctx = new WorkflowContextStub(data);

        when(repository.findById(LoanFacilityId.of(command.loanFacilityId()))).thenReturn(Optional.of(facility));
        when(facility.revertAddCollateral(command.collateralSerials(), clock))
                .thenReturn(Result.success(Unit.INSTANCE));
        when(facility.getId()).thenReturn(LoanFacilityId.of(command.loanFacilityId()));
        when(facility.domainEvents()).thenReturn(List.of());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(repository).save(facility);
    }

    private static CompensateCollateralCommand command() {
        return CompensateCollateralCommand.builder()
                .uid(UUID.randomUUID())
                .version(0L)
                .loanFacilityId(UUID.randomUUID())
                .collateralSerials(List.of("SER-1"))
                .build();
    }
}
