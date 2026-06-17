package ir.dotin.loan.trade.core.application.service.cancelfacility.step;

import java.time.LocalDate;
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
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver.LoanIdentifiers;
import ir.dotin.loan.trade.core.application.service.cancelfacility.commandhandler.CancelFacilityCommandHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class CancelFacilityStepTest {

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private ApplicationNumberResolver applicationNumberResolver;

    @InjectMocks
    private CancelFacilityStep step;

    @Test
    void executeFailsWhenApplicationNumberMissing() {
        when(applicationNumberResolver.resolveByApplicationNumber("APP-1")).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(facilityRepository, never()).save(ArgumentMatchers.any());
    }

    @Test
    void executeFailsWhenFacilityNotFound() {
        LoanIdentifiers ids = new LoanIdentifiers(UUID.randomUUID(), UUID.randomUUID());
        when(applicationNumberResolver.resolveByApplicationNumber("APP-1")).thenReturn(Optional.of(ids));
        when(facilityRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx());

        assertThat(result.isFailure()).isTrue();
        verify(facilityRepository, never()).save(ArgumentMatchers.any());
    }

    private static WorkflowContextStub<CancelFacilityCommandHandler.Data> ctx() {
        CancelFacilityCommand command = new CancelFacilityCommand(
                UUID.randomUUID(), 0L, UUID.randomUUID(), "APP-1", LocalDate.of(2026, 1, 1), "desc", "reason", "TXN-1");
        return new WorkflowContextStub<>(
                "cancel-facility", new CancelFacilityCommandHandler.Data(command, Unit.INSTANCE));
    }
}
