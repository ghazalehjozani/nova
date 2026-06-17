package ir.dotin.loan.trade.core.application.service.approvefacility.step;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.approvefacility.commandhandler.ApproveFacilityCommandHandler;
import ir.dotin.loan.trade.core.application.service.approvefacility.commandhandler.ApproveFacilityCommandHandler.ApprovalPreparation;
import ir.dotin.loan.trade.core.application.service.approvefacility.factory.ApprovalStrategyFactory;
import ir.dotin.loan.trade.core.application.service.approvefacility.strategy.ApprovalStrategy;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApproveFacilityStep implements PublishingWriteActivity<ApproveFacilityCommandHandler.Data> {

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final ApprovalStrategyFactory strategyFactory;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<ApproveFacilityCommandHandler.Data> ctx) {
        ApproveFacilityCommand command = ctx.data().command();
        ApprovalPreparation prepared = ctx.data().prepared();
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        var result = Result.fromOptional(
                        loanFacilityRepository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> Result.fromOptional(
                                loanArrangementRepository.findById(facility.getLoanArrangementId()),
                                () -> FailureCause.notFound(Notification.ofError(
                                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                                        facility.getLoanArrangementId())))
                        .flatMap(arrangement -> {
                            ApprovalStrategy strategy = strategyFactory.getStrategy(command);
                            return strategy.validate(command, facility, arrangement)
                                    .flatMap(ignored -> strategy.approve(
                                            command,
                                            facility,
                                            arrangement,
                                            prepared.confirmType(),
                                            prepared.sanctionDetails()))
                                    .map(ignored -> {
                                        loanFacilityRepository.save(facility, command.version());
                                        log.debug("Facility approved: {}", command.loanFacilityId());
                                        return facility.domainEvents();
                                    });
                        }));

        return StepResult.fromWriteResult(result);
    }
}
