package ir.dotin.loan.trade.core.application.service.originateloanfacility.step;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.DependencyLoader;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityBuilder;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityPersister;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.OriginationPreparation;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.InstallmentScheduleStrategy;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.InstallmentScheduleStrategySelector;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.TradeLoanFacilityValidationService;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class OriginateFacilityStep implements PublishingWriteActivity<OriginateFacilityData> {

    private final DependencyLoader dependencyLoader;
    private final InstallmentScheduleStrategySelector strategySelector;
    private final FacilityBuilder facilityBuilder;
    private final TradeLoanFacilityValidationService validationService;
    private final FacilityPersister facilityPersister;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<OriginateFacilityData> ctx) {
        OriginateFacilityData data = ctx.data();
        return StepResult.fromWriteResult(write(data.command(), data.prepared()));
    }

    private Result<List<DomainEvent<?>>> write(OriginateLoanFacilityCommand command, OriginationPreparation prepared) {
        return dependencyLoader
                .loadDependencies(command, prepared.partyInfos())
                .flatMap(context -> executeOriginationWorkflow(command, context, prepared.applicationNumber()));
    }

    private Result<List<DomainEvent<?>>> executeOriginationWorkflow(
            OriginateLoanFacilityCommand command, FacilityOriginationContext context, ApplicationNumber appNumber) {

        LoanFacilityId facilityId = LoanFacilityId.generate();
        InstallmentScheduleStrategy strategy = strategySelector.selectStrategy(
                requireNonNull(context.arrangement().getInstallmentPolicy()).installmentPaymentType());

        return strategy.validateCommand(command)
                .flatMap(valid -> prepareSchedule(command, strategy, context, facilityId, appNumber))
                .flatMap(scheduleOpt -> assembleFacility(command, context, scheduleOpt, facilityId, appNumber))
                .flatMap(this::persistResult);
    }

    private Result<Optional<InstallmentSchedule>> prepareSchedule(
            OriginateLoanFacilityCommand command,
            InstallmentScheduleStrategy strategy,
            FacilityOriginationContext context,
            LoanFacilityId facilityId,
            ApplicationNumber appNumber) {

        Result<TradeLoanApplication> draftAppResult = facilityBuilder.buildApplication(command, context, appNumber);
        if (draftAppResult.isFailure()) {
            return Result.failure(draftAppResult.err().orElseThrow());
        }

        return strategy.planSchedule(command, draftAppResult.unwrap(), context, facilityId);
    }

    private Result<FacilityAggregation> assembleFacility(
            OriginateLoanFacilityCommand command,
            FacilityOriginationContext context,
            Optional<InstallmentSchedule> scheduleOpt,
            LoanFacilityId facilityId,
            ApplicationNumber appNumber) {

        InstallmentScheduleId scheduleId = scheduleOpt
                .map(InstallmentSchedule::getId)
                .orElse(InstallmentScheduleId.generate().unwrap());

        return facilityBuilder
                .buildFacility(command, context, scheduleId, facilityId, appNumber)
                .flatMap(facility -> validationService
                        .validateForCreation(facility, context.arrangement(), context.loanType())
                        .map(valid -> new FacilityAggregation(facility, scheduleOpt)));
    }

    private Result<List<DomainEvent<?>>> persistResult(FacilityAggregation aggregation) {
        return facilityPersister
                .persist(aggregation.facility(), aggregation.schedule())
                .map(facilityPersister::aggregateEvents);
    }

    private record FacilityAggregation(TradeLoanFacility facility, Optional<InstallmentSchedule> schedule) {}
}
