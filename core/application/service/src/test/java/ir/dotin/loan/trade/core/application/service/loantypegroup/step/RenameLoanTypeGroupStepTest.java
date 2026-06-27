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
import ir.dotin.loan.trade.core.application.ports.inbound.command.RenameLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.RenameLoanTypeGroupData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RenameLoanTypeGroupStepTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private LoanTypeGroupRepository repository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowContext<RenameLoanTypeGroupData> ctx;

    private RenameLoanTypeGroupStep step;

    @BeforeEach
    void setUp() {
        step = new RenameLoanTypeGroupStep(repository, clock);
    }

    private RenameLoanTypeGroupCommand command(UUID groupId) {
        return RenameLoanTypeGroupCommand.builder()
                .uid(UUID.randomUUID())
                .version(0L)
                .groupId(groupId)
                .title("نام جدید")
                .build();
    }

    private LoanTypeGroup persisted(UUID groupId) {
        return LoanTypeGroup.reconstitute(
                new LoanTypeGroupId(groupId), Title.of("نام قبلی").unwrap(), null, 0L);
    }

    @Test
    void renamesExistingGroupAndEmitsRenamedEvent() {
        UUID groupId = UUID.randomUUID();
        LoanTypeGroup group = persisted(groupId);
        when(ctx.data()).thenReturn(new RenameLoanTypeGroupData(command(groupId)));
        when(repository.findById(any())).thenReturn(Optional.of(group));

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        List<DomainEvent<?>> events = result.getOutputOrNull();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getClass().getSimpleName()).isEqualTo("LoanTypeGroupRenamed");
        verify(repository).save(group, 0L);
    }

    @Test
    void failsWhenGroupNotFound() {
        when(ctx.data()).thenReturn(new RenameLoanTypeGroupData(command(UUID.randomUUID())));
        when(repository.findById(any())).thenReturn(Optional.empty());

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.hasBusinessErrorCode("GROUP_NOT_FOUND")).isTrue();
        verify(repository, never()).save(any());
    }
}
