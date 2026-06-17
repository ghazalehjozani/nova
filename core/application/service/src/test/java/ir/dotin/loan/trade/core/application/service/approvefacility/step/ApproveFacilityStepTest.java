package ir.dotin.loan.trade.core.application.service.approvefacility.step;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.approvefacility.commandhandler.ApproveFacilityCommandHandler;
import ir.dotin.loan.trade.core.application.service.approvefacility.commandhandler.ApproveFacilityCommandHandler.ApprovalPreparation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ApproveFacilityStepTest {

    @Mock
    private TradeLoanFacilityRepository loanFacilityRepository;

    @InjectMocks
    private ApproveFacilityStep step;

    @Test
    void executeFailsWhenFacilityNotFound() {
        when(loanFacilityRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(loanFacilityRepository, never()).save(ArgumentMatchers.any());
        verify(loanFacilityRepository, never()).save(ArgumentMatchers.any(), ArgumentMatchers.anyLong());
    }

    private static WorkflowContextStub<ApproveFacilityCommandHandler.Data> ctx() {
        ApproveFacilityCommand command = ApproveFacilityCommand.builder()
                .uid(UUID.randomUUID())
                .version(0L)
                .loanFacilityId(UUID.randomUUID())
                .confirmType("MANUAL")
                .build();
        ApprovalPreparation prepared = new ApprovalPreparation(new ConfirmType("MANUAL"), null);
        return new WorkflowContextStub<>("approve-facility", new ApproveFacilityCommandHandler.Data(command, prepared));
    }
}
