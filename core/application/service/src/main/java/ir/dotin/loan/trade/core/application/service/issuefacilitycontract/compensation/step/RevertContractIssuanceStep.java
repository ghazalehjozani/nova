package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.compensation.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.WriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateContractIssuanceCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RevertContractIssuanceStep implements WriteActivity<RevertContractIssuanceData> {

    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<RevertContractIssuanceData> ctx) {
        RevertContractIssuanceData data = ctx.data();
        return StepResult.fromWriteResult(write(data.command(), data.prepared()));
    }

    private Result<List<DomainEvent<?>>> write(
            CompensateContractIssuanceCommand command, ReversalPreparation prepared) {
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> facility.revertContractIssuance(clock).map(transactionNumber -> {
                    prepared.reversals().set(transactionNumber);
                    return facility;
                }))
                .onSuccess(repository::save)
                .map(TradeLoanFacility::domainEvents);
    }
}
