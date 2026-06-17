package ir.dotin.loan.trade.core.application.service.collectinstallment.step;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver.LoanIdentifiers;
import ir.dotin.loan.trade.core.application.service.collectinstallment.commandhandler.CollectInstallmentCommandHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class CollectInstallmentStepTest {

    @Mock
    private InstallmentScheduleRepository installmentScheduleRepository;

    @Mock
    private ApplicationNumberResolver applicationNumberResolver;

    @InjectMocks
    private CollectInstallmentStep step;

    @Test
    void executeFailsWhenApplicationNumberMissing() {
        when(applicationNumberResolver.resolveByApplicationNumber("APP-1")).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(installmentScheduleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void executeFailsWhenScheduleNotFound() {
        LoanIdentifiers ids = new LoanIdentifiers(UUID.randomUUID(), UUID.randomUUID());
        when(applicationNumberResolver.resolveByApplicationNumber("APP-1")).thenReturn(Optional.of(ids));
        when(installmentScheduleRepository.findById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(installmentScheduleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private static WorkflowContextStub<CollectInstallmentCommandHandler.Data> ctx() {
        CollectInstallmentCommand command = new CollectInstallmentCommand(
                UUID.randomUUID(), 0L, "APP-1", "REF-1", "IRR", List.of(), null, null, null);
        return new WorkflowContextStub<>(
                "collect-installment",
                new CollectInstallmentCommandHandler.Data(
                        command, ir.dotin.platform.pangaea.commons.core.Unit.INSTANCE));
    }
}
