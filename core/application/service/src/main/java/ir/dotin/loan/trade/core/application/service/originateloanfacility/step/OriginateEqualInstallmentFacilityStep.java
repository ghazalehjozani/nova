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
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.ScheduleSource;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ProductProfile;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateEqualInstallmentFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.DependencyLoader;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityBuilder;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityPersister;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.OriginationPreparation;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.OriginationSchedulePlanner;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.ProductProfileResolver;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.TradeLoanFacilityValidationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OriginateEqualInstallmentFacilityStep
        implements PublishingWriteActivity<OriginateEqualInstallmentFacilityData> {

    private final DependencyLoader dependencyLoader;
    private final ProductProfileResolver profileResolver;
    private final FacilityBuilder facilityBuilder;
    private final OriginationSchedulePlanner schedulePlanner;
    private final TradeLoanFacilityValidationService validationService;
    private final FacilityPersister facilityPersister;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<OriginateEqualInstallmentFacilityData> ctx) {
        OriginateEqualInstallmentFacilityData data = ctx.data();
        return StepResult.fromWriteResult(write(data.command(), data.prepared()));
    }

    private Result<List<DomainEvent<?>>> write(
            OriginateEqualInstallmentFacilityCommand command, OriginationPreparation prepared) {

        return dependencyLoader
                .loadDependencies(command, prepared.partyInfos())
                .flatMap(context -> profileResolver
                        .resolveFor(context, ScheduleSource.SYSTEM)
                        .flatMap(profile -> originate(command, context, profile, prepared.applicationNumber())));
    }

    private Result<List<DomainEvent<?>>> originate(
            OriginateEqualInstallmentFacilityCommand command,
            FacilityOriginationContext context,
            ProductProfile profile,
            ApplicationNumber appNumber) {

        LoanFacilityId facilityId = LoanFacilityId.generate();

        return facilityBuilder
                .buildApplication(command, context, profile, appNumber)
                .flatMap(application -> schedulePlanner.planSystemGenerated(application, context, profile, facilityId))
                .flatMap(schedule -> facilityBuilder
                        .buildFacility(command, context, profile, schedule.getId(), facilityId, appNumber)
                        .flatMap(facility -> validationService
                                .validateForCreation(facility, context.arrangement(), context.loanType())
                                .map(valid -> new FacilityAggregation(facility, schedule))))
                .flatMap(this::persist);
    }

    private Result<List<DomainEvent<?>>> persist(FacilityAggregation aggregation) {
        return facilityPersister
                .persist(aggregation.facility(), Optional.of(aggregation.schedule()))
                .map(facilityPersister::aggregateEvents);
    }

    private record FacilityAggregation(TradeLoanFacility facility, InstallmentSchedule schedule) {}
}
