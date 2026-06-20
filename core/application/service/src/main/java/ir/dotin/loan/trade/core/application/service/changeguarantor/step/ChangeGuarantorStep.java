package ir.dotin.loan.trade.core.application.service.changeguarantor.step;

import java.time.Clock;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Verdict;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.specification.ChangeGuarantorPercentageSpecification;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.changeguarantor.commandhandler.ChangeGuarantorCommandHandler;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeGuarantorStep implements PublishingWriteActivity<ChangeGuarantorCommandHandler.Data> {

    private final TradeLoanFacilityRepository facilityRepository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<ChangeGuarantorCommandHandler.Data> ctx) {
        var data = ctx.data();

        var result = loadFacility(LoanFacilityId.of(data.facilityId())).flatMap(facility -> {
            Set<GuarantorParty> newGuarantors = Set.copyOf(data.guarantors());
            return facility.changeGuarantors(newGuarantors, clock)
                    .flatMap(ignored -> verifyPercentages(facility))
                    .map(ignored -> {
                        List<DomainEvent<?>> events = List.copyOf(facility.domainEvents());
                        facilityRepository.save(facility, data.expectedVersion());
                        log.info("Changed guarantors to {} for facility: {}", newGuarantors.size(), data.facilityId());
                        return events;
                    });
        });

        return StepResult.fromWriteResult(result);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<Boolean> verifyPercentages(TradeLoanFacility facility) {
        Verdict verdict = new ChangeGuarantorPercentageSpecification().isSatisfiedBy(facility);
        return verdict.isSatisfied() ? Result.success(true) : Result.failure(verdict.reasons());
    }
}
