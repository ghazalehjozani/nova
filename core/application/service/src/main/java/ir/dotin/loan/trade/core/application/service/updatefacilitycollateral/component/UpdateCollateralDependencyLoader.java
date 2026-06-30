package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.platform.pangaea.commons.core.context.ContextSnapshot;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
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
public class UpdateCollateralDependencyLoader {

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository arrangementRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final CollateralCalculationService collateralCalculationService;
    private final AbstractCollateralValidationService<TradeLoanFacility, TradeLoanArrangement>
            collateralValidationService;
    private final CollateralReadPort collateralReadPort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<Unit> validateAndCheck(LoanFacilityId loanFacilityId, List<Collateral> newCollaterals) {

        // Step 1: Load facility and perform status guard before any parallel I/O
        Result<TradeLoanFacility> facilityResult = Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId)));
        if (facilityResult.isFailure()) {
            return Result.failure(facilityResult.err().orElseThrow());
        }
        TradeLoanFacility facility = facilityResult.unwrap();

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

        // Build lookup structures for serial-set checks
        Map<CollateralSerial, Collateral> existingBySerial = facility.getCollaterals().stream()
                .collect(Collectors.toMap(Collateral::collateralSerial, c -> c, (a, b) -> a));

        Set<CollateralSerial> requestSerials =
                newCollaterals.stream().map(Collateral::collateralSerial).collect(Collectors.toSet());

        // Step 2: No new serials — every serial in the request must already exist on the facility
        for (Collateral c : newCollaterals) {
            if (!existingBySerial.containsKey(c.collateralSerial())) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.COLLATERAL_SERIAL_NOT_IN_FACILITY,
                        c.collateralSerial().value()));
            }
        }

        // Step 3: No missing serials — every serial currently on the facility must appear in the request
        for (CollateralSerial existingSerial : existingBySerial.keySet()) {
            if (!requestSerials.contains(existingSerial)) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.COLLATERAL_SERIAL_MISSING_FROM_REQUEST,
                        existingSerial.value()));
            }
        }

        // Step 4: No zero or negative amounts
        for (Collateral c : newCollaterals) {
            if (c.usedAmount().value().signum() <= 0) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.COLLATERAL_AMOUNT_MUST_BE_POSITIVE,
                        c.collateralSerial().value()));
            }
        }

        // Step 5: Identify increased serials (newAmount > oldAmount)
        // existingBySerial.get() is non-null here: step 2 guarantees every request serial is present
        List<Collateral> increased = newCollaterals.stream()
                .filter(c -> {
                    Collateral existing = Objects.requireNonNull(existingBySerial.get(c.collateralSerial()));
                    return c.usedAmount().isGreaterThan(existing.usedAmount()).unwrap();
                })
                .toList();

        // Step 6: Parallel fan-out — arrangement + schedule always; collateral details only for increased serials
        CompletableFuture<Result<TradeLoanArrangement>> arrangementFuture =
                CompletableFuture.supplyAsync(() -> loadArrangement(facility), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        CompletableFuture<Result<Optional<InstallmentSchedule>>> scheduleFuture =
                CompletableFuture.supplyAsync(() -> loadSchedule(facility), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));

        CompletableFuture<Result<List<CollateralDetails>>> collateralDetailsFuture;
        if (increased.isEmpty()) {
            collateralDetailsFuture = CompletableFuture.completedFuture(Result.success(List.of()));
        } else {
            List<Supplier<Result<CollateralDetails>>> collateralTasks = increased.stream()
                    .map(c -> (Supplier<Result<CollateralDetails>>) () -> loadCollateralDetails(c.collateralSerial()))
                    .toList();
            collateralDetailsFuture = CompletableFuture.supplyAsync(
                    () -> ParallelFanout.allOf(collateralTasks), ContextSnapshot.wrap(VIRTUAL_EXECUTOR));
        }

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

        TradeLoanArrangement arrangement = arrangementResult.unwrap();
        Optional<InstallmentSchedule> schedule = scheduleResult.unwrap();

        // Step 7: Per-collateral adequacy check — only for increased serials
        if (!increased.isEmpty()) {
            List<CollateralDetails> collateralDetails = collateralDetailsResult.unwrap();
            Map<CollateralSerial, CollateralDetails> detailsMap = new HashMap<>();
            for (int i = 0; i < increased.size(); i++) {
                detailsMap.put(increased.get(i).collateralSerial(), collateralDetails.get(i));
            }

            for (Collateral collateral : increased) {
                CollateralDetails details = detailsMap.get(collateral.collateralSerial());
                if (details == null) {
                    return Result.failure(Notification.ofError(
                            TradeLoanApplicationServiceErrors.COLLATERAL_DETAILS_NOT_FOUND,
                            collateral.collateralSerial().value()));
                }
                Money realCollateralPrice = Money.valueOf(details.price(), arrangement.getCurrencyType())
                        .unwrap();
                if (collateral.usedAmount().isGreaterThan(realCollateralPrice).unwrap()) {
                    log.warn(
                            "Collateral adequacy validation failed for serial {}",
                            collateral.collateralSerial().value());
                    return Result.failure(
                            TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE,
                            realCollateralPrice,
                            collateral.usedAmount());
                }
            }
        }

        // Step 8: Total-value floor check
        Money totalValue = newCollaterals.stream()
                .map(Collateral::usedAmount)
                .reduce(
                        Money.zero(arrangement.getCurrencyType()).unwrap(),
                        (a, b) -> a.add(b).unwrap());

        Result<Money> requiredAmountResult =
                collateralCalculationService.calculateNeededCollateral(facility, arrangement, schedule.orElse(null));
        if (requiredAmountResult.isFailure())
            return Result.failure(requiredAmountResult.err().orElseThrow());
        Money requiredAmount = requiredAmountResult.unwrap();

        if (totalValue.isLessThan(requiredAmount).unwrap()) {
            log.warn("Total collateral value {} is less than required amount {}", totalValue, requiredAmount);
            return Result.failure(
                    TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE, totalValue, requiredAmount);
        }

        // Step 9: Domain-level individual collateral validation
        Result<?> domainResult =
                collateralValidationService.validateIndividualCollaterals(facility, arrangement, newCollaterals);
        if (domainResult.isFailure()) return Result.failure(domainResult.err().orElseThrow());

        // Step 10: FCB assurance-file validation — only for increased serials
        if (!increased.isEmpty()) {
            List<CollateralSerial> serials =
                    increased.stream().map(Collateral::collateralSerial).toList();
            List<Long> usedCosts = increased.stream()
                    .map(c -> c.usedAmount().value().longValue())
                    .toList();
            BranchCode branchCode = facility.getLoanApplication().getBranch().code();

            Result<CollateralValidation> validationRes =
                    collateralReadPort.validateAddAssuranceToFile(serials, usedCosts, branchCode);
            if (validationRes.isFailure()) {
                return Result.failure(validationRes.err().orElseThrow());
            }
            CollateralValidation validation = validationRes.unwrap();
            if (!validation.isValid()) {
                return Result.failure(
                        TradeLoanApplicationServiceErrors.COLLATERAL_VALIDATION_FAILED,
                        Objects.requireNonNullElse(validation.message(), ""));
            }
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

    private Result<CollateralDetails> loadCollateralDetails(CollateralSerial serial) {
        return collateralReadPort.loadCollateral(serial.value(), "");
    }
}
