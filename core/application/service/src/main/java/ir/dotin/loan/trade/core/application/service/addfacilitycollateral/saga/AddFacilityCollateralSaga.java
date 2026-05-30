package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.saga;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.saga.api.annotation.SagaHandler;
import ir.dotin.platform.pangaea.saga.api.context.SagaContext;
import ir.dotin.platform.pangaea.saga.api.definition.SagaDefinition;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.platform.pangaea.saga.api.definition.SagaStep;
import ir.dotin.platform.pangaea.saga.api.definition.SagaSteps;
import ir.dotin.platform.pangaea.saga.api.model.ResultStepAdapter;
import ir.dotin.platform.pangaea.saga.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.AbstractCollateralValidationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCollateralAdded;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

/**
 * Saga orchestrating the distributed add-facility-collateral operation (LN-59321). Replaces the previous hand-rolled
 * reserve / rollback / try-catch in {@code AddFacilityCollateralCommandHandler}.
 *
 * <p>Read-only pre-validation (dependency loading, adequacy and total-value checks) stays in the command handler before
 * this saga runs. Two mutating steps:
 *
 * <ol>
 *   <li>{@code RESERVE_COLLATERALS} — remote reservation of every collateral; compensation un-reserves them.
 *   <li>{@code ADD_COLLATERAL} — local domain mutation + persistence; compensation reverts the addition.
 * </ol>
 *
 * Each step re-loads the aggregate from the repository (instance-safe; the aggregate is never stashed in saga data).
 */
@SagaHandler
@Component
@RequiredArgsConstructor
public class AddFacilityCollateralSaga implements SagaDefinition<AddFacilityCollateralSagaData> {

    private static final Logger log = LoggerFactory.getLogger(AddFacilityCollateralSaga.class);

    private static final Integer RESERVE_DURATION_MINUTES = 1440;

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository arrangementRepository;
    private final TradeLoanFacilityService domainService;
    private final AbstractCollateralValidationService collateralValidationService;
    private final CollateralServicePort collateralServicePort;
    private final AddFacilityCollateralCommandMapper mapper;
    private final Clock clock;

    @Override
    public String sagaType() {
        return "add-facility-collateral";
    }

    @Override
    public List<SagaStep<AddFacilityCollateralSagaData, ?>> steps() {
        return List.of(
                SagaSteps.step(
                                AddFacilityCollateralStep.RESERVE_COLLATERALS,
                                this::reserveCollaterals,
                                this::unReserveCollaterals)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(30)),
                SagaSteps.step(AddFacilityCollateralStep.ADD_COLLATERAL, this::addCollateral, this::revertAddCollateral)
                        .withNoRetry());
    }

    @Override
    public AddFacilityCollateralSagaData createInitialData(SagaInput input) {
        var collateralInput = (AddFacilityCollateralInput) input;
        return AddFacilityCollateralSagaData.initial(
                collateralInput.facilityId(), collateralInput.requestId(), collateralInput.collaterals());
    }

    private StepResult<List<String>> reserveCollaterals(SagaContext<AddFacilityCollateralSagaData> ctx) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return new StepResult.Failure<>(facilityResult.err().orElseThrow());
        }
        var facility = facilityResult.unwrap();

        if (facility.getLoanApplication().getApplicationNumber().isEmpty()) {
            return new StepResult.Failure<>(FailureCause.businessRule(
                    Notification.ofError(TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING)));
        }
        ApplicationNumber appNumber =
                facility.getLoanApplication().getApplicationNumber().get();

        List<Collateral> collaterals = mapper.toCollaterals(data.collaterals());
        List<String> reserved = new ArrayList<>();

        for (Collateral collateral : collaterals) {
            Result<List<CollateralSerial>> result = collateralServicePort.reserveCollateral(
                    collateral.collateralSerial(),
                    appNumber,
                    data.requestId(),
                    RESERVE_DURATION_MINUTES,
                    collateral.usedAmount());

            if (result.isFailure()) {
                // partial reservation within this step is not compensated by the framework — clean up here.
                unReserve(appNumber, reserved, data.requestId());
                return new StepResult.Failure<>(result.err().orElseThrow());
            }
            reserved.add(collateral.collateralSerial().value());
        }

        ctx.updateSagaData(d -> d.withReservedSerials(reserved));
        log.info("Reserved {} collaterals for facility: {}", reserved.size(), data.facilityId());
        return new StepResult.Success<>(reserved);
    }

    private StepResult<Void> unReserveCollaterals(
            SagaContext<AddFacilityCollateralSagaData> ctx, List<String> reservedSerials) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }
        var facility = facilityResult.unwrap();

        if (facility.getLoanApplication().getApplicationNumber().isEmpty()) {
            log.warn("No application number for facility {}; skipping un-reserve", data.facilityId());
            return new StepResult.Success<>(null);
        }
        ApplicationNumber appNumber =
                facility.getLoanApplication().getApplicationNumber().get();

        unReserve(appNumber, reservedSerials, data.requestId());
        log.warn("Un-reserved {} collaterals for facility: {}", reservedSerials.size(), data.facilityId());
        return new StepResult.Success<>(null);
    }

    private StepResult<Void> addCollateral(SagaContext<AddFacilityCollateralSagaData> ctx) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }
        var facility = facilityResult.unwrap();

        var arrangementResult = loadArrangement(facility);
        if (arrangementResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(arrangementResult);
        }
        var arrangement = arrangementResult.unwrap();

        List<Collateral> collaterals = mapper.toCollaterals(data.collaterals());

        var addResult = domainService.addCollateral(facility, collaterals);
        if (addResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(addResult);
        }

        var validation = collateralValidationService.validateFacilityCollaterals(facility, arrangement);
        if (validation.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(validation);
        }

        List<AddFacilityCollateralSagaData.CapturedEventData> captured = facility.domainEvents().stream()
                .filter(TradeLoanFacilityCollateralAdded.class::isInstance)
                .map(TradeLoanFacilityCollateralAdded.class::cast)
                .map(event -> new AddFacilityCollateralSagaData.CapturedEventData(
                        event.eventId(),
                        event.aggregateId(),
                        event.eventType(),
                        event.sanctionedLoanId(),
                        event.collateralSerials(),
                        event.createdAt()))
                .toList();
        ctx.updateSagaData(d -> d.withCapturedEvents(captured));

        facilityRepository.save(facility);
        log.info("{} collaterals added for facility: {}", collaterals.size(), data.facilityId());
        return new StepResult.Success<>(null);
    }

    private StepResult<Void> revertAddCollateral(SagaContext<AddFacilityCollateralSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }
        var facility = facilityResult.unwrap();

        List<String> serials = data.collaterals().stream()
                .map(AddFacilityCollateralCommand.CollateralDto::collateralSerial)
                .toList();

        var revertResult = facility.revertAddCollateral(serials, clock);
        if (revertResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(revertResult);
        }

        facilityRepository.save(facility);
        log.warn("Reverted add-collateral for facility: {}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private void unReserve(ApplicationNumber appNumber, List<String> serials, UUID requestId) {
        for (String serial : serials) {
            collateralServicePort.unReserveCollateral(
                    CollateralSerial.of(serial).unwrap(), appNumber, UUID.randomUUID(), requestId);
        }
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<TradeLoanArrangement> loadArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                arrangementRepository.findById(facility.getLoanArrangementId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                        facility.getLoanArrangementId(),
                        facility.getId().value())));
    }
}
