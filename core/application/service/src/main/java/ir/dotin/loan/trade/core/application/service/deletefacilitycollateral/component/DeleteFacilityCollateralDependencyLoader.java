package ir.dotin.loan.trade.core.application.service.deletefacilitycollateral.component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.context.ContextSnapshot;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.CollateralCalculationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
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
public class DeleteFacilityCollateralDependencyLoader {

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository arrangementRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final CollateralCalculationService collateralCalculationService;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<Unit> validateAndCheck(LoanFacilityId loanFacilityId, List<String> serialsToDelete) {

        // Step 1: Load facility
        Result<TradeLoanFacility> facilityResult = Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId)));
        if (facilityResult.isFailure()) {
            return Result.failure(facilityResult.err().orElseThrow());
        }
        TradeLoanFacility facility = facilityResult.unwrap();

        // Step 2: Status guard — only these three states allow collateral modification
        FacilityStatus state = facility.getCurrentState();
        if (state != FacilityStatus.APPROVED
                && state != FacilityStatus.ISSUE_CONTRACT
                && state != FacilityStatus.FULLY_DISBURSED
                && state != FacilityStatus.PARTIALLY_DISBURSED) {
            return Result.failure(FailureCause.businessRule(Notification.ofError(
                    TradeLoanApplicationServiceErrors.FACILITY_INVALID_STATE_FOR_COLLATERAL_UPDATE,
                    facility.getId().value(),
                    state)));
        }

        // Step 3: Serial existence check — every serial to delete must already be attached to the facility
        Set<String> existingSerials = facility.getCollaterals().stream()
                .map(c -> c.collateralSerial().value())
                .collect(Collectors.toSet());

        for (String serial : serialsToDelete) {
            if (!existingSerials.contains(serial)) {
                log.warn("Collateral serial {} not found in facility {}", serial, loanFacilityId.value());
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.COLLATERAL_NOT_FOUND_IN_FACILITY,
                        serial,
                        loanFacilityId.value()));
            }
        }

        // Step 4: Load arrangement and schedule in parallel (needed for value-floor calculation)
        CompletableFuture<Result<TradeLoanArrangement>> arrangementFuture =
                CompletableFuture.supplyAsync(() -> loadArrangement(facility), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        CompletableFuture<Result<Optional<InstallmentSchedule>>> scheduleFuture =
                CompletableFuture.supplyAsync(() -> loadSchedule(facility), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        Result<TradeLoanArrangement> arrangementResult = arrangementFuture.join();
        Result<Optional<InstallmentSchedule>> scheduleResult = scheduleFuture.join();

        Notification aggregatedNotification = Notification.create();
        if (arrangementResult.isFailure())
            aggregatedNotification.merge(arrangementResult.err().orElseThrow().notification());
        if (scheduleResult.isFailure())
            aggregatedNotification.merge(scheduleResult.err().orElseThrow().notification());

        if (aggregatedNotification.hasErrors()) {
            return Result.failure(aggregatedNotification);
        }

        TradeLoanArrangement arrangement = arrangementResult.unwrap();
        Optional<InstallmentSchedule> schedule = scheduleResult.unwrap();

        // Step 5: Compute remaining collateral value after deletion
        Set<String> deleteSet = Set.copyOf(serialsToDelete);
        Money remainingValue = facility.getCollaterals().stream()
                .filter(c -> !deleteSet.contains(c.collateralSerial().value()))
                .map(Collateral::usedAmount)
                .reduce(
                        Money.zero(arrangement.getCurrencyType()).unwrap(),
                        (a, b) -> a.add(b).unwrap());

        // Step 6: Value-floor check — remaining value must meet the calculated minimum
        Result<Money> requiredAmountResult =
                collateralCalculationService.calculateNeededCollateral(facility, arrangement, schedule.orElse(null));
        if (requiredAmountResult.isFailure())
            return Result.failure(requiredAmountResult.err().orElseThrow());

        Money requiredAmount = requiredAmountResult.unwrap();
        if (remainingValue.isLessThan(requiredAmount).unwrap()) {
            log.warn(
                    "Remaining collateral value {} after deletion is less than required amount {}",
                    remainingValue,
                    requiredAmount);
            return Result.failure(
                    TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE, remainingValue, requiredAmount);
        }

        return Result.success();
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
}
