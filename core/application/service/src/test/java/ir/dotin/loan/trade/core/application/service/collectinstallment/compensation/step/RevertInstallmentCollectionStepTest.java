package ir.dotin.loan.trade.core.application.service.collectinstallment.compensation.step;

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
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver.LoanIdentifiers;
import ir.dotin.loan.trade.core.application.service.collectinstallment.compensation.commandhandler.CompensateCollectInstallmentCommandHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RevertInstallmentCollectionStepTest {

    @Mock
    private ApplicationNumberResolver applicationNumberResolver;

    @Mock
    private InstallmentScheduleRepository installmentScheduleRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private RevertInstallmentCollectionStep step;

    @Test
    void executeFailsWhenApplicationNumberMissing() {
        when(applicationNumberResolver.resolveByApplicationNumber("APP-1")).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(installmentScheduleRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void executeFailsWhenScheduleNotFound() {
        LoanIdentifiers ids = new LoanIdentifiers(UUID.randomUUID(), UUID.randomUUID());
        when(applicationNumberResolver.resolveByApplicationNumber("APP-1")).thenReturn(Optional.of(ids));
        when(installmentScheduleRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(installmentScheduleRepository, never()).save(ArgumentMatchers.any());
    }

    private static WorkflowContextStub<CompensateCollectInstallmentCommandHandler.Data> ctx() {
        CompensateCollectInstallmentCommand command =
                new CompensateCollectInstallmentCommand(UUID.randomUUID(), 0L, "APP-1", List.of("TXN-1"));
        return new WorkflowContextStub<>(
                "compensate-collect-installment",
                new CompensateCollectInstallmentCommandHandler.Data(command, Unit.INSTANCE));
    }
}
