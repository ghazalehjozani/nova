package ir.dotin.loan.trade.core.application.service.loantypegroup.step;

import java.time.Clock;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.error.LoanTypeGroupErrors;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.vo.LoanTypeGroupCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CreateLoanTypeGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.LoanTypeGroupRepository;
import ir.dotin.loan.trade.core.application.service.loantypegroup.data.CreateLoanTypeGroupData;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateLoanTypeGroupStep implements PublishingWriteActivity<CreateLoanTypeGroupData> {

    private final LoanTypeGroupRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<CreateLoanTypeGroupData> ctx) {
        return StepResult.fromWriteResult(write(ctx.data().command()));
    }

    private Result<List<DomainEvent<?>>> write(CreateLoanTypeGroupCommand command) {
        @Nullable
        LoanTypeGroupId parentId =
                command.parentGroupId() == null ? null : new LoanTypeGroupId(command.parentGroupId());
        if (parentId != null && !repository.existsById(parentId)) {
            return Result.failure(LoanTypeGroupErrors.PARENT_NOT_FOUND, parentId);
        }
        return LoanTypeGroupCode.of(command.code())
                .flatMap(code -> {
                    if (repository.existsByCode(code)) {
                        return Result.<LoanTypeGroup>failure(LoanTypeGroupErrors.CODE_ALREADY_EXISTS, code.value());
                    }
                    return Title.of(command.title())
                            .flatMap(title -> LoanTypeGroup.create(code, title, parentId, clock));
                })
                .onSuccess(repository::save)
                .map(LoanTypeGroup::domainEvents);
    }
}
