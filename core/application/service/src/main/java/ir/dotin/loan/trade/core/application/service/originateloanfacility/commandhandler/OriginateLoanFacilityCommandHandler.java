package ir.dotin.loan.trade.core.application.service.originateloanfacility.commandhandler;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.CustomerInfoLoader;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.DependencyLoader;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityBuilder;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityPersister;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityValidator;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.PartyEligibilityValidator;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.InstallmentScheduleStrategy;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.InstallmentScheduleStrategySelector;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.TradeLoanFacilityValidationService;

import lombok.extern.slf4j.Slf4j;

import static ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel.DIGITAL_BANK;
import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public final class OriginateLoanFacilityCommandHandler
        extends WorkflowCommandHandler<OriginateLoanFacilityCommand, OriginateLoanFacilityCommandHandler.Data> {

    @Override
    protected Workflow<Data> route(WorkflowRoute<Data> route) {
        return route.singleWrite(
                "originate-loan-facility",
                ctx -> StepResult.fromWriteResult(
                        write(ctx.data().command(), ctx.data().prepared())));
    }

    @Override
    protected Result<Data> seed(OriginateLoanFacilityCommand command) {
        return prepare(command).map(prepared -> new Data(command, prepared));
    }

    private Result<OriginationPreparation> prepare(OriginateLoanFacilityCommand command) {
        log.info("Starting facility origination for LoanType: {}", command.loanTypeCode());

        Result<Unit> validationResult = facilityValidator.callAndValidateServices(command);
        if (validationResult.isFailure()) {
            return Result.failure(validationResult.err().orElseThrow());
        }

        Result<List<PartyInfoResponse>> partyInfosResult = customerInfoLoader.loadPartyInfos(command);
        if (partyInfosResult.isFailure()) {
            return Result.failure(partyInfosResult.err().orElseThrow());
        }
        List<PartyInfoResponse> partyInfos = partyInfosResult.unwrap();

        if (command.loanApplication().applicantChannel() != DIGITAL_BANK) {
            Result<Unit> eligibilityResult = partyEligibilityValidator.validate(partyInfos);
            if (eligibilityResult.isFailure()) {
                return Result.failure(eligibilityResult.err().orElseThrow());
            }
        }

        return facilityBuilder
                .resolveApplicationNumber(command, partyInfos)
                .map(applicationNumber -> new OriginationPreparation(partyInfos, applicationNumber));
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

    record Data(OriginateLoanFacilityCommand command, OriginationPreparation prepared) {}

    public record OriginationPreparation(List<PartyInfoResponse> partyInfos, ApplicationNumber applicationNumber) {}

    private record FacilityAggregation(TradeLoanFacility facility, Optional<InstallmentSchedule> schedule) {}

    private final FacilityValidator facilityValidator;
    private final CustomerInfoLoader customerInfoLoader;
    private final PartyEligibilityValidator partyEligibilityValidator;
    private final DependencyLoader dependencyLoader;
    private final InstallmentScheduleStrategySelector strategySelector;
    private final FacilityBuilder facilityBuilder;
    private final TradeLoanFacilityValidationService validationService;
    private final FacilityPersister facilityPersister;

    public OriginateLoanFacilityCommandHandler(
            WorkflowEngine engine,
            FacilityValidator facilityValidator,
            CustomerInfoLoader customerInfoLoader,
            PartyEligibilityValidator partyEligibilityValidator,
            DependencyLoader dependencyLoader,
            InstallmentScheduleStrategySelector strategySelector,
            FacilityBuilder facilityBuilder,
            TradeLoanFacilityValidationService validationService,
            FacilityPersister facilityPersister) {
        super(engine);
        this.facilityValidator = facilityValidator;
        this.customerInfoLoader = customerInfoLoader;
        this.partyEligibilityValidator = partyEligibilityValidator;
        this.dependencyLoader = dependencyLoader;
        this.strategySelector = strategySelector;
        this.facilityBuilder = facilityBuilder;
        this.validationService = validationService;
        this.facilityPersister = facilityPersister;
    }
}
