package ir.dotin.loan.trade.core.application.service.assignloantypegroup.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loantype.entity.AbstractLoanType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RemoveLoanTypeFromGroupCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.assignloantypegroup.data.RemoveLoanTypeFromGroupData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RemoveLoanTypeFromGroupStep implements PublishingWriteActivity<RemoveLoanTypeFromGroupData> {

    private final TradeLoanTypeRepository loanTypeRepository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<RemoveLoanTypeFromGroupData> ctx) {
        RemoveLoanTypeFromGroupCommand command = ctx.data().command();
        LoanTypeId loanTypeId = LoanTypeId.of(command.loanTypeId());

        Result<List<DomainEvent<?>>> result = loanTypeRepository
                .findById(loanTypeId)
                .map(loanType -> loanType.removeFromGroup(clock)
                        .onSuccess(removed -> loanTypeRepository.save(loanType, command.version()))
                        .map(AbstractLoanType::domainEvents))
                .orElseGet(() -> Result.failure(TradeLoanApplicationServiceErrors.LOAN_TYPE_NOT_FOUND, loanTypeId));
        return StepResult.fromWriteResult(result);
    }
}
