package ir.dotin.loan.trade.core.application.service.originateloanfacility.orchestrator;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.DependencyLoader;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityBuilder;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityPersister;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.InstallmentScheduleStrategy;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.InstallmentScheduleStrategySelector;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.TradeLoanFacilityValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityOriginationOrchestrator {

    private final DependencyLoader dependencyLoader;
    private final InstallmentScheduleStrategySelector strategySelector;
    private final FacilityBuilder facilityBuilder;
    private final TradeLoanFacilityValidationService validationService;
    private final FacilityPersister facilityPersister;

    public Result<List<DomainEvent<?, ?>>> originate(OriginateLoanFacilityCommand command) {
        log.info("Starting facility origination process");

        return dependencyLoader.loadDependencies(command).flatMap(context -> {
            InstallmentScheduleStrategy strategy = strategySelector.selectStrategy(
                    context.arrangement().getInstallmentPolicy().installmentPaymentType());

            return strategy.validateCommand(command)
                    .flatMap(ignored -> planSchedule(command, strategy, context))
                    .flatMap(scheduleOpt -> createAndValidateFacility(command, context, scheduleOpt))
                    .flatMap((FacilityWithSchedule facility) ->
                            facilityPersister.persist(facility.facility(), facility.schedule()))
                    .map(facilityPersister::aggregateEvents);
        });
    }

    private Result<Optional<InstallmentSchedule>> planSchedule(
            OriginateLoanFacilityCommand command,
            InstallmentScheduleStrategy strategy,
            FacilityOriginationContext context) {

        TradeLoanApplication tempApplication = facilityBuilder.buildApplication(command, context, null);

        return strategy.planSchedule(command, tempApplication, context);
    }

    private Result<FacilityWithSchedule> createAndValidateFacility(
            OriginateLoanFacilityCommand command,
            FacilityOriginationContext context,
            Optional<InstallmentSchedule> scheduleOpt) {

        return facilityBuilder
                .buildFacility(
                        command,
                        context,
                        scheduleOpt.map(InstallmentSchedule::getId).orElse(null))
                .flatMap(facility -> validateFacility(facility, context)
                        .map(validatedFacility -> new FacilityWithSchedule(validatedFacility, scheduleOpt)));
    }

    private Result<TradeLoanFacility> validateFacility(TradeLoanFacility facility, FacilityOriginationContext context) {
        return validationService
                .validateForCreation(facility, context.arrangement(), context.loanType())
                .mapNonNull(ignored -> facility);
    }

    private record FacilityWithSchedule(TradeLoanFacility facility, Optional<InstallmentSchedule> schedule) {}
}
