package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.platform.pangaea.commons.core.context.ContextSnapshot;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.CollateralCalculationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.AbstractCollateralValidationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
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
    private final AbstractCollateralValidationService collateralValidationService;
    private final CollateralReadPort collateralReadPort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<CollateralValidationContext> loadAndCalculate(
            LoanFacilityId loanFacilityId, List<Collateral> collaterals) {

        Result<TradeLoanFacility> facilityResult = Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId)));
        if (facilityResult.isFailure()) {
            return Result.failure(facilityResult.err().orElseThrow());
        }
        TradeLoanFacility facility = facilityResult.unwrap();

        // Heterogeneous singles (arrangement, schedule) run as wrapped branches; the homogeneous per-collateral loads
        // fan out via ParallelFanout (run on its own branch to stay concurrent with the singles).
        CompletableFuture<Result<TradeLoanArrangement>> arrangementFuture =
                CompletableFuture.supplyAsync(() -> loadArrangement(facility), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        CompletableFuture<Result<Optional<InstallmentSchedule>>> scheduleFuture =
                CompletableFuture.supplyAsync(() -> loadSchedule(facility), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        List<Supplier<Result<CollateralDetails>>> collateralTasks = collaterals.stream()
                .map(c -> (Supplier<Result<CollateralDetails>>) () -> loadCollateralDetails(c.collateralSerial()))
                .toList();
        CompletableFuture<Result<List<CollateralDetails>>> collateralDetailsFuture = CompletableFuture.supplyAsync(
                () -> ParallelFanout.allOf(collateralTasks), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        Result<TradeLoanArrangement> arrangementResult = arrangementFuture.join();
        Result<Optional<InstallmentSchedule>> scheduleResult = scheduleFuture.join();
        Result<List<CollateralDetails>> collateralDetailsResult = collateralDetailsFuture.join();

        Notification aggregatedNotification = Notification.create();
        if (arrangementResult.isFailure())
            aggregatedNotification.merge(arrangementResult.err().orElseThrow().notification());
        if (scheduleResult.isFailure())
            aggregatedNotification.merge(scheduleResult.err().orElseThrow().notification());
        if (collateralDetailsResult.isFailure())
            aggregatedNotification.merge(
                    collateralDetailsResult.err().orElseThrow().notification());

        if (aggregatedNotification.hasErrors()) {
            return Result.failure(aggregatedNotification);
        }

        List<CollateralDetails> collateralDetails = collateralDetailsResult.unwrap();
        Map<CollateralSerial, CollateralDetails> detailsMap = new HashMap<>();
        for (int i = 0; i < collaterals.size(); i++) {
            detailsMap.put(collaterals.get(i).collateralSerial(), collateralDetails.get(i));
        }

        Result<Money> requiredAmountResult = collateralCalculationService.calculateNeededCollateral(
                facility, arrangementResult.unwrap(), scheduleResult.unwrap().orElse(null));
        if (requiredAmountResult.isFailure())
            return Result.failure(requiredAmountResult.err().orElseThrow());

        Result<?> domainCollateralValidationResult = collateralValidationService.validateIndividualCollaterals(
                facility, arrangementResult.unwrap(), collaterals);
        if (domainCollateralValidationResult.isFailure())
            return Result.failure(domainCollateralValidationResult.err().orElseThrow());

        Money calculatedRequiredAmount = requiredAmountResult.unwrap();

        List<CollateralSerial> serials =
                collaterals.stream().map(Collateral::collateralSerial).toList();
        List<Long> usedCosts = collaterals.stream()
                .map(c -> c.usedAmount().value().longValue())
                .toList();

        Result<CollateralValidation> validationRes = validateAssurance(
                serials, usedCosts, facility.getLoanApplication().getBranch().code());

        if (validationRes.isFailure()) {
            return Result.failure(validationRes.err().orElseThrow());
        }

        CollateralValidation validation = validationRes.unwrap();
        if (!validation.isValid()) {
            return Result.failure(
                    TradeLoanApplicationServiceErrors.COLLATERAL_VALIDATION_FAILED,
                    Objects.requireNonNullElse(validation.message(), ""));
        }

        return Result.success(new CollateralValidationContext(
                facility,
                arrangementResult.unwrap(),
                scheduleResult.unwrap(),
                calculatedRequiredAmount,
                detailsMap,
                new CollateralValidation(true, "")));
    }

    private Result<TradeLoanArrangement> loadArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                arrangementRepository.findById(facility.getLoanArrangementId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                        facility.getLoanArrangementId())));
    }

    private Result<Optional<InstallmentSchedule>> loadSchedule(TradeLoanFacility facility) {
        return Result.success(facility.getInstallmentScheduleId().flatMap(installmentScheduleRepository::findById));
    }

    private Result<CollateralDetails> loadCollateralDetails(CollateralSerial serial) {
        return collateralReadPort.loadCollateral(serial.value(), "");
    }

    private Result<CollateralValidation> validateAssurance(
            List<CollateralSerial> serials, List<Long> usedCosts, BranchCode branchCode) {
        return collateralReadPort.validateAddAssuranceToFile(serials, usedCosts, branchCode);
    }
}
