package ir.dotin.loan.trade.core.application.service.assignloantypegroup.step;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RemoveLoanTypeFromGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.assignloantypegroup.data.RemoveLoanTypeFromGroupData;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RemoveLoanTypeFromGroupStepTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private TradeLoanTypeRepository loanTypeRepository;

    @Mock
    private TradeLoanType loanType;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowContext<RemoveLoanTypeFromGroupData> ctx;

    private RemoveLoanTypeFromGroupStep step;

    @BeforeEach
    void setUp() {
        step = new RemoveLoanTypeFromGroupStep(loanTypeRepository, clock);
    }

    private RemoveLoanTypeFromGroupCommand command() {
        return RemoveLoanTypeFromGroupCommand.builder()
                .uid(UUID.randomUUID())
                .version(0L)
                .loanTypeId(UUID.randomUUID())
                .build();
    }

    @Test
    void removesLoanTypeFromGroupAndSaves() {
        when(ctx.data()).thenReturn(new RemoveLoanTypeFromGroupData(command()));
        when(loanTypeRepository.findById(any())).thenReturn(Optional.of(loanType));
        doReturn(Result.success(loanType)).when(loanType).removeFromGroup(any());
        doReturn(List.<DomainEvent<?>>of()).when(loanType).domainEvents();

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(loanTypeRepository).save(loanType, 0L);
    }

    @Test
    void failsWhenLoanTypeNotFound() {
        when(ctx.data()).thenReturn(new RemoveLoanTypeFromGroupData(command()));
        when(loanTypeRepository.findById(any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.hasBusinessErrorCode("LOAN_TYPE_NOT_FOUND")).isTrue();
        verify(loanTypeRepository, never()).save(any());
    }
}
