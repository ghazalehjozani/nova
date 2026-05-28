package ir.dotin.loan.trade.core.application.service.originateloanfacility.orchestrator;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.ResolvedPartyDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.DependencyLoader;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityBuilder;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityPersister;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper.ResolvedPartyMapper;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.InstallmentScheduleStrategy;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.InstallmentScheduleStrategySelector;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.TradeLoanFacilityValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityOriginationOrchestrator {

    private final DependencyLoader dependencyLoader;
    private final InstallmentScheduleStrategySelector strategySelector;
    private final FacilityBuilder facilityBuilder;
    private final TradeLoanFacilityValidationService validationService;
    private final FacilityPersister facilityPersister;
    private final ResolvedPartyMapper resolvedPartyMapper;

    /**
     * Lean transactional origination: persists only. FCB validation + customer-info loading already ran tx-free in the
     * pre-flight ({@code PrepareFacilityOriginationQuery}); their result is threaded in via
     * {@code command.resolvedParties()}. This handler holds the pooled Hikari connection across the DB work only —
     * never across a remote FCB round-trip.
     */
    public Result<List<DomainEvent<?>>> originate(OriginateLoanFacilityCommand command) {
        log.info("Starting facility origination persistence for LoanType: {}", command.loanTypeCode());

        List<ResolvedPartyDto> resolvedParties = command.resolvedParties();
        if (resolvedParties == null || resolvedParties.isEmpty()) {
            throw new IllegalStateException("Origination command dispatched without pre-flight resolvedParties — "
                    + "PrepareFacilityOriginationQuery must run before the command");
        }

        Result<List<PartyInfoResponse>> partyInfosResult = resolvedPartyMapper.reconstruct(resolvedParties);
        if (partyInfosResult.isFailure()) {
            return Result.failure(partyInfosResult.err().orElseThrow());
        }

        return dependencyLoader
                .loadDependencies(command, partyInfosResult.unwrap())
                .flatMap(context -> executeOriginationWorkflow(command, context));
    }

    private Result<List<DomainEvent<?>>> executeOriginationWorkflow(
            OriginateLoanFacilityCommand command, FacilityOriginationContext context) {

        LoanFacilityId facilityId = LoanFacilityId.generate();
        InstallmentScheduleStrategy strategy = strategySelector.selectStrategy(
                requireNonNull(context.arrangement().getInstallmentPolicy()).installmentPaymentType());

        return strategy.validateCommand(command)
                .flatMap(valid -> prepareSchedule(command, strategy, context, facilityId))
                .flatMap(scheduleOpt -> assembleFacility(command, context, scheduleOpt, facilityId))
                .flatMap(this::persistResult);
    }

    private Result<Optional<InstallmentSchedule>> prepareSchedule(
            OriginateLoanFacilityCommand command,
            InstallmentScheduleStrategy strategy,
            FacilityOriginationContext context,
            LoanFacilityId facilityId) {

        Result<TradeLoanApplication> draftAppResult = facilityBuilder.buildApplication(command, context);
        if (draftAppResult.isFailure()) {
            return Result.failure(draftAppResult.err().orElseThrow());
        }

        return strategy.planSchedule(command, draftAppResult.unwrap(), context, facilityId);
    }

    private Result<FacilityAggregation> assembleFacility(
            OriginateLoanFacilityCommand command,
            FacilityOriginationContext context,
            Optional<InstallmentSchedule> scheduleOpt,
            LoanFacilityId facilityId) {

        InstallmentScheduleId scheduleId = scheduleOpt
                .map(InstallmentSchedule::getId)
                .orElse(InstallmentScheduleId.generate().unwrap());

        return facilityBuilder
                .buildFacility(command, context, scheduleId, facilityId)
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
