package ir.dotin.loan.trade.core.application.service.loanfacilityrestructuring.step;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.InstallmentRecalculationService;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LoanFacilityRestructuringCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.loanfacilityrestructuring.commandhandler.LoanFacilityRestructuringCommandHandler;
import ir.dotin.loan.trade.core.application.service.loanfacilityrestructuring.mapper.LoanFacilityRestructuringInstallmentSchedulePlanMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RestructureFacilityStepTest {

    @Mock
    private TradeLoanFacilityRepository tradeLoanFacilityRepository;

    @Mock
    private InstallmentScheduleRepository installmentScheduleRepository;

    @Mock
    private ApplicationNumberResolver applicationNumberResolver;

    @Mock
    private LoanFacilityRestructuringInstallmentSchedulePlanMapper schedulePlanMapper;

    @Mock
    private InstallmentRecalculationService recalculationService;

    @Mock
    private Clock clock;

    @InjectMocks
    private RestructureFacilityStep step;

    @Test
    void executeFailsWhenApplicationNumberMissing() {
        when(applicationNumberResolver.resolveLoanFacilityIdByApplicationNumber("APP-1"))
                .thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(tradeLoanFacilityRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void executeFailsWhenFacilityNotFound() {
        when(applicationNumberResolver.resolveLoanFacilityIdByApplicationNumber("APP-1"))
                .thenReturn(Optional.of(LoanFacilityId.of(UUID.randomUUID())));
        when(tradeLoanFacilityRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(tradeLoanFacilityRepository, never()).save(ArgumentMatchers.any());
    }

    private static WorkflowContextStub<LoanFacilityRestructuringCommandHandler.Data> ctx() {
        LoanFacilityRestructuringCommand command =
                new LoanFacilityRestructuringCommand(UUID.randomUUID(), 0L, "APP-1", "user-1", "REF-1", 12, null);
        return new WorkflowContextStub<>(
                "loan-facility-restructuring",
                new LoanFacilityRestructuringCommandHandler.Data(command, Unit.INSTANCE));
    }
}
