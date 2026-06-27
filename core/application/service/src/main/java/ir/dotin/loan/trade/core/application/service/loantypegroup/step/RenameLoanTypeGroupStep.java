package ir.dotin.loan.trade.core.application.service.loantypegroup.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.error.LoanTypeGroupErrors;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RenameLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.RenameLoanTypeGroupData;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RenameLoanTypeGroupStep implements PublishingWriteActivity<RenameLoanTypeGroupData> {

    private final LoanTypeGroupRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<RenameLoanTypeGroupData> ctx) {
        RenameLoanTypeGroupCommand command = ctx.data().command();
        LoanTypeGroupId id = new LoanTypeGroupId(command.groupId());
        Result<List<DomainEvent<?>>> result = repository
                .findById(id)
                .map(group -> Title.of(command.title())
                        .flatMap(title -> group.rename(title, clock))
                        .onSuccess(renamed -> repository.save(renamed, command.version()))
                        .map(LoanTypeGroup::domainEvents))
                .orElseGet(() -> Result.failure(LoanTypeGroupErrors.GROUP_NOT_FOUND, id));
        return StepResult.fromWriteResult(result);
    }
}
