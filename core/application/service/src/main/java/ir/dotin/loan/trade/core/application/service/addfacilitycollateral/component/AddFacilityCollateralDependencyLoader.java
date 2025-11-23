package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.CollateralCalculationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.i18n.AddFacilityCollateralErrorCodes;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddFacilityCollateralDependencyLoader {

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository arrangementRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final CollateralCalculationService collateralCalculationService;
    private final CollateralServicePort collateralServicePort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<CollateralValidationContext> loadAndCalculate(
            LoanFacilityId loanFacilityId, CollateralSerial collateralSerial) {

        Result<TradeLoanFacility> facilityResult = Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                Notification.ofError(AddFacilityCollateralErrorCodes.FACILITY_NOT_FOUND, loanFacilityId));
        if (facilityResult.isFailure()) {
            return Result.failure(facilityResult.notification());
        }
        TradeLoanFacility facility = facilityResult.value();

        CompletableFuture<Result<TradeLoanArrangement>> arrangementFuture =
                CompletableFuture.supplyAsync(() -> loadArrangement(facility), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Optional<InstallmentSchedule>>> scheduleFuture =
                CompletableFuture.supplyAsync(() -> loadSchedule(facility), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<CollateralDetails>> collateralFuture = CompletableFuture.supplyAsync(
                () -> loadCollateralDetails(collateralSerial, facility), VIRTUAL_EXECUTOR);

        CompletableFuture.allOf(arrangementFuture, scheduleFuture, collateralFuture)
                .join();

        Result<TradeLoanArrangement> arrangementResult = arrangementFuture.join();
        Result<Optional<InstallmentSchedule>> scheduleResult = scheduleFuture.join();
        Result<CollateralDetails> collateralRes = collateralFuture.join();

        Notification aggregatedNotification = Notification.create();
        aggregatedNotification.merge(arrangementResult.notification());
        aggregatedNotification.merge(scheduleResult.notification());
        aggregatedNotification.merge(collateralRes.notification());

        if (aggregatedNotification.hasErrors()) {
            return Result.failure(aggregatedNotification);
        }

        Result<Money> requiredAmountResult = collateralCalculationService.calculateNeededCollateral(
                facility, arrangementResult.value(), scheduleResult.value().orElse(null));
        if (requiredAmountResult.isFailure()) return Result.failure(requiredAmountResult.notification());

        Money calculatedRequiredAmount = requiredAmountResult.value();

        Long usedCost = calculatedRequiredAmount.value().longValue();

        Result<CollateralValidation> validationRes = validateAssurance(collateralSerial, usedCost);

        if (validationRes.isFailure()) {
            return Result.failure(validationRes.notification());
        }

        CollateralValidation validation = validationRes.value();
        if (!validation.isValid()) {
            return Result.failure(Notification.ofError(
                    AddFacilityCollateralErrorCodes.COLLATERAL_VALIDATION_FAILED, validation.message()));
        }

        return Result.success(new CollateralValidationContext(
                facility,
                arrangementResult.value(),
                scheduleResult.value(),
                calculatedRequiredAmount,
                collateralRes.value(),
                validation));
    }

    private Result<TradeLoanArrangement> loadArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                arrangementRepository.findById(facility.getLoanArrangementId()),
                Notification.ofError(
                        AddFacilityCollateralErrorCodes.LOAN_ARRANGEMENT_NOT_FOUND, facility.getLoanArrangementId()));
    }

    private Result<Optional<InstallmentSchedule>> loadSchedule(TradeLoanFacility facility) {
        return Result.success(facility.getInstallmentScheduleId().flatMap(installmentScheduleRepository::findById));
    }

    private Result<CollateralDetails> loadCollateralDetails(CollateralSerial serial, TradeLoanFacility facility) {
        return collateralServicePort.loadCollateral(
                serial.value(), facility.getId().value().toString());
    }

    private Result<CollateralValidation> validateAssurance(CollateralSerial serial, Long usedCost) {
        return collateralServicePort.validateAddAssuranceToFile(List.of(serial), List.of(usedCost));
    }
}
