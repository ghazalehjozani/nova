package ir.dotin.loan.trade.core.application.service.loantypegroup.step;

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

import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.inbound.command.MoveLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.MoveLoanTypeGroupData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class MoveLoanTypeGroupStepTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private LoanTypeGroupRepository repository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowContext<MoveLoanTypeGroupData> ctx;

    private MoveLoanTypeGroupStep step;

    @BeforeEach
    void setUp() {
        step = new MoveLoanTypeGroupStep(repository, clock);
    }

    private MoveLoanTypeGroupCommand command(UUID groupId, UUID newParentGroupId) {
        return MoveLoanTypeGroupCommand.builder()
                .uid(UUID.randomUUID())
                .version(0L)
                .groupId(groupId)
                .newParentGroupId(newParentGroupId)
                .build();
    }

    private LoanTypeGroup persisted(UUID groupId) {
        return LoanTypeGroup.reconstitute(
                new LoanTypeGroupId(groupId), Title.of("گروه").unwrap(), null, 0L);
    }

    @Test
    void reParentsAndEmitsMovedEvent() {
        UUID groupId = UUID.randomUUID();
        UUID newParent = UUID.randomUUID();
        LoanTypeGroup group = persisted(groupId);
        when(ctx.data()).thenReturn(new MoveLoanTypeGroupData(command(groupId, newParent)));
        when(repository.existsById(any())).thenReturn(true);
        when(repository.findAncestorChain(any())).thenReturn(List.of());
        when(repository.findById(any())).thenReturn(Optional.of(group));

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        List<DomainEvent<?>> events = result.getOutputOrNull();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getClass().getSimpleName()).isEqualTo("LoanTypeGroupMoved");
        verify(repository).save(group, 0L);
    }

    @Test
    void failsWhenNewParentDoesNotExist() {
        when(ctx.data()).thenReturn(new MoveLoanTypeGroupData(command(UUID.randomUUID(), UUID.randomUUID())));
        when(repository.existsById(any())).thenReturn(false);

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.hasBusinessErrorCode("PARENT_NOT_FOUND")).isTrue();
        verify(repository, never()).save(any());
    }

    @Test
    void failsWhenMoveWouldCreateCycle() {
        UUID groupId = UUID.randomUUID();
        when(ctx.data()).thenReturn(new MoveLoanTypeGroupData(command(groupId, UUID.randomUUID())));
        when(repository.existsById(any())).thenReturn(true);
        when(repository.findAncestorChain(any())).thenReturn(List.of(new LoanTypeGroupId(groupId)));

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.hasBusinessErrorCode("CYCLE_NOT_ALLOWED")).isTrue();
        verify(repository, never()).save(any());
    }

    @Test
    void failsWhenGroupNotFound() {
        when(ctx.data()).thenReturn(new MoveLoanTypeGroupData(command(UUID.randomUUID(), null)));
        when(repository.findById(any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.hasBusinessErrorCode("GROUP_NOT_FOUND")).isTrue();
        verify(repository, never()).save(any());
    }
}
