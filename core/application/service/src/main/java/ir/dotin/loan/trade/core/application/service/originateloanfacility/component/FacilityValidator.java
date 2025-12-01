package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityValidator {

    private final DepositServicePort depositServicePort;
    private final LoanServicePort loanServicePort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<Void> callAndValidateServices(OriginateLoanFacilityCommand command) {
        log.debug("Call and validate services for facility origination");

        var futures = List.of(
                runAsync(() -> validateDeposit(command)),
                runAsync(() -> isDepositClosed(command)),
                runAsync(() -> validateEconomicalSector(command)),
                runAsync(() -> validateEconomicalSectionForLoanType(command)),
                runAsync(() -> validateDebtorDeposit(command)),
                runAsync(() -> validateCreditorDeposit(command)),
                runAsync(() -> validateSubSource(command)),
                runAsync(() -> validateDepositCurrency(command)),
                runAsync(() -> validateRequestReason(command)));

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        Notification aggregatedNotification = Notification.create();
        for (var future : futures) {
            aggregatedNotification.merge(future.join().notification());
        }

        return aggregatedNotification.hasErrors() ? Result.failure(aggregatedNotification) : Result.success();
    }

    private CompletableFuture<Result<Void>> runAsync(Supplier<Result<Void>> supplier) {
        return CompletableFuture.supplyAsync(supplier, VIRTUAL_EXECUTOR);
    }

    private Result<Void> validateDeposit(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        Result<DepositInfo> result = getDepositInfo(depositNumber);

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        return Result.success();
    }

    private Result<Void> validateEconomicalSector(OriginateLoanFacilityCommand command) {
        String code = command.loanApplication().economicSector().code();
        Result<EconomicSector> result = loadEconomicalSectorByCode(code);

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        return Result.success();
    }

    private Result<Void> validateEconomicalSectionForLoanType(OriginateLoanFacilityCommand command) {
        String sectorCode = command.loanApplication().economicSector().code();
        String typeCodeRaw = command.loanTypeCode();
        LoanTypeCode loanTypeCode = LoanTypeCode.of(typeCodeRaw).getValue();

        Result<EconomicalSectorValidation> result = validateEconomicalSectorForLoanType(sectorCode, loanTypeCode);

        return validateBusinessRule(
                result,
                EconomicalSectorValidation::isValid,
                () -> Notification.ofError(
                        OriginateLoanFacilityErrorCodes.INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE,
                        sectorCode,
                        loanTypeCode));
    }

    private Result<Void> isDepositClosed(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        String currency = command.loanApplication().currency().value();

        Result<DepositClosedStatus> result = isDepositClosed(depositNumber, currency);

        return validateBusinessRule(
                result,
                status -> !status.isClosed(),
                () -> Notification.ofError(
                        OriginateLoanFacilityErrorCodes.DISBURSE_DESTINATION_DEPOSIT_IS_CLOSED, depositNumber));
    }

    private Result<Void> validateDebtorDeposit(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        String currency = command.loanApplication().currency().value();

        Result<DebtorDepositValidation> result = validateDebtorDeposit(depositNumber, currency);

        return validateBusinessRule(
                result,
                DebtorDepositValidation::isValidDebtorDeposit,
                () -> Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_DEBTOR_DEPOSIT, depositNumber));
    }

    private Result<Void> validateCreditorDeposit(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        String currency = command.loanApplication().currency().value();
        BigDecimal amount = command.loanApplication().requestedAmount().value();

        Result<CreditorDepositValidation> result = validateCreditorDeposit(depositNumber, currency, amount);

        return validateBusinessRule(
                result,
                CreditorDepositValidation::isValidCreditorDeposit,
                () -> Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_CREDITOR_DEPOSIT, depositNumber));
    }

    private Result<Void> validateSubSource(OriginateLoanFacilityCommand command) {
        String code = command.loanApplication().subSource().code();
        Result<SubSource> result = loadResourceByCode(code);

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        return Result.success();
    }

    private Result<Void> validateDepositCurrency(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        String currency = command.loanApplication().currency().value();

        Result<CurrencyValidation> result = hasDepositAllowedCurrencies(depositNumber, currency);

        return validateBusinessRule(
                result,
                CurrencyValidation::isValid,
                () -> Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_DEPOSIT_CURRENCY, depositNumber));
    }

    private Result<Void> validateRequestReason(OriginateLoanFacilityCommand command) {
        String code = command.loanApplication().requestReason().code();
        Result<ReasonType> result = loadRequestReasonByCode(code);

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        return Result.success();
    }

    private <T> Result<Void> validateBusinessRule(
            Result<T> result, Predicate<T> isValid, Supplier<Notification> errorSupplier) {

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        if (!isValid.test(result.value())) {
            return Result.failure(errorSupplier.get());
        }

        return Result.success();
    }

    private Result<DepositInfo> getDepositInfo(String depositNumber) {
        return depositServicePort.getDepositInfo(
                DepositNumber.valueOf(depositNumber).getValue());
    }

    private Result<CurrencyValidation> hasDepositAllowedCurrencies(String depositNumber, String currencyCode) {
        return depositServicePort.hasDepositAllowedCurrencies(
                DepositNumber.valueOf(depositNumber).getValue(),
                List.of(Objects.requireNonNull(
                        CurrencyType.valueOf(currencyCode).value())));
    }

    private Result<DebtorDepositValidation> validateDebtorDeposit(String depositNumber, String currencyCode) {
        return depositServicePort.validateDebtorDeposit(
                DepositNumber.valueOf(depositNumber).getValue(),
                CurrencyType.valueOf(currencyCode).getValue());
    }

    private Result<CreditorDepositValidation> validateCreditorDeposit(
            String depositNumber, String currencyCode, BigDecimal amount) {
        return depositServicePort.validateCreditorDeposit(
                DepositNumber.valueOf(depositNumber).getValue(),
                CurrencyType.valueOf(currencyCode).getValue(),
                amount);
    }

    private Result<SubSource> loadResourceByCode(String subSourceCode) {
        return loanServicePort.loadResourceByCode(subSourceCode);
    }

    private Result<ReasonType> loadRequestReasonByCode(String requestReasonCode) {
        return loanServicePort.loadReasonTypeForCreate(requestReasonCode);
    }

    private Result<DepositClosedStatus> isDepositClosed(String depositNumber, String currencyCode) {
        return depositServicePort.isDepositClosed(
                DepositNumber.valueOf(depositNumber).getValue(),
                CurrencyType.valueOf(currencyCode).getValue());
    }

    private Result<EconomicSector> loadEconomicalSectorByCode(String economicSectorCode) {
        return loanServicePort.loadEconomicalSectorByCode(
                EconomicSector.of(economicSectorCode).getValue());
    }

    private Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
            String economicSectorCode, LoanTypeCode loanTypeCode) {
        return loanServicePort.validateEconomicalSectorForLoanType(
                EconomicSector.of(economicSectorCode).getValue(), loanTypeCode);
    }

    // TODO reasonType service must change load-reason-type
}
