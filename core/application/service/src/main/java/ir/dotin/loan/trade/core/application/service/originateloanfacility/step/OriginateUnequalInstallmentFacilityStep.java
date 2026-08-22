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
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.ScheduleSource;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ProductProfile;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.LoanFacilityInstallmentAmountValidator;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateUnequalInstallmentFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.DependencyLoader;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityBuilder;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityPersister;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.OriginationPreparation;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.OriginationSchedulePlanner;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.ProductProfileResolver;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper.InstallmentSchedulePlanMapper;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.TradeLoanFacilityValidationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OriginateUnequalInstallmentFacilityStep
        implements PublishingWriteActivity<OriginateUnequalInstallmentFacilityData> {

    private final DependencyLoader dependencyLoader;
    private final ProductProfileResolver profileResolver;
    private final FacilityBuilder facilityBuilder;
    private final OriginationSchedulePlanner schedulePlanner;
    private final InstallmentSchedulePlanMapper schedulePlanMapper;
    private final LoanFacilityInstallmentAmountValidator installmentAmountValidator;
    private final TradeLoanFacilityValidationService validationService;
    private final FacilityPersister facilityPersister;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<OriginateUnequalInstallmentFacilityData> ctx) {
        OriginateUnequalInstallmentFacilityData data = ctx.data();
        return StepResult.fromWriteResult(write(data.command(), data.prepared()));
    }

    private Result<List<DomainEvent<?>>> write(
            OriginateUnequalInstallmentFacilityCommand command, OriginationPreparation prepared) {

        return dependencyLoader
                .loadDependencies(command, prepared.partyInfos())
                .flatMap(context -> profileResolver
                        .resolveFor(context, ScheduleSource.USER_ON_ORIGINATION)
                        .flatMap(profile -> originate(command, context, profile, prepared.applicationNumber())));
    }

    private Result<List<DomainEvent<?>>> originate(
            OriginateUnequalInstallmentFacilityCommand command,
            FacilityOriginationContext context,
            ProductProfile profile,
            ApplicationNumber appNumber) {

        LoanFacilityId facilityId = LoanFacilityId.generate();

        return facilityBuilder
                .buildApplication(command, context, profile, appNumber)
                .flatMap(application -> planSchedule(command, application, context, profile, facilityId))
                .flatMap(planned -> facilityBuilder
                        .buildFacility(
                                command, context, profile, planned.schedule().getId(), facilityId, appNumber)
                        .flatMap(facility -> validationService
                                .validateForCreation(facility, context.arrangement(), context.loanType())
                                .map(valid -> new FacilityAggregation(facility, planned.schedule()))))
                .flatMap(this::persist);
    }

    private Result<PlannedSchedule> planSchedule(
            OriginateUnequalInstallmentFacilityCommand command,
            TradeLoanApplication application,
            FacilityOriginationContext context,
            ProductProfile profile,
            LoanFacilityId facilityId) {

        List<InstallmentSpec> specs = schedulePlanMapper.mapSpecs(
                command.installmentSchedulePlan().installments(), application.getCurrency());

        return schedulePlanner
                .planFromSuppliedTable(application, context, profile, facilityId, specs)
                .flatMap(schedule -> installmentAmountValidator
                        .validate(application, schedule)
                        .map(valid -> new PlannedSchedule(application, schedule)));
    }

    private Result<List<DomainEvent<?>>> persist(FacilityAggregation aggregation) {
        return facilityPersister
                .persist(aggregation.facility(), Optional.of(aggregation.schedule()))
                .map(facilityPersister::aggregateEvents);
    }

    private record PlannedSchedule(TradeLoanApplication application, InstallmentSchedule schedule) {}

    private record FacilityAggregation(TradeLoanFacility facility, InstallmentSchedule schedule) {}
}
