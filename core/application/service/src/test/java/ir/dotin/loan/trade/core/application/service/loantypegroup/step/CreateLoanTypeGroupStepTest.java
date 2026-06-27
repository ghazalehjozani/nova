package ir.dotin.loan.trade.core.application.service.loantypegroup.step;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CreateLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.CreateLoanTypeGroupData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class CreateLoanTypeGroupStepTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private LoanTypeGroupRepository repository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowContext<CreateLoanTypeGroupData> ctx;

    private CreateLoanTypeGroupStep step;

    @BeforeEach
    void setUp() {
        step = new CreateLoanTypeGroupStep(repository, clock);
    }

    @Test
    void createsRootGroupAndEmitsCreatedEvent() {
        CreateLoanTypeGroupCommand command = CreateLoanTypeGroupCommand.builder()
                .uid(UUID.randomUUID())
                .title("تسهیلات")
                .build();
        when(ctx.data()).thenReturn(new CreateLoanTypeGroupData(command));

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        List<DomainEvent<?>> events = result.getOutputOrNull();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getClass().getSimpleName()).isEqualTo("LoanTypeGroupCreated");
        verify(repository).save(any(LoanTypeGroup.class));
    }

    @Test
    void failsWhenParentDoesNotExist() {
        CreateLoanTypeGroupCommand command = CreateLoanTypeGroupCommand.builder()
                .uid(UUID.randomUUID())
                .title("زیرگروه")
                .parentGroupId(UUID.randomUUID())
                .build();
        when(ctx.data()).thenReturn(new CreateLoanTypeGroupData(command));
        when(repository.existsById(any())).thenReturn(false);

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.hasBusinessErrorCode("PARENT_NOT_FOUND")).isTrue();
        verify(repository, never()).save(any());
    }
}
