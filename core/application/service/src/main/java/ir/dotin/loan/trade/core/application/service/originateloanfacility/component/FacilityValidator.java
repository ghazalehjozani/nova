package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.NotificationError;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityValidator {

    private final CustomerServicePort customerServicePort;
    private final DepositServicePort depositServicePort;
    private final LoanServicePort loanServicePort;
    private final TradeLoanTypeRepository tradeLoanTypeRepository;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<Void> callAndValidateServices(OriginateLoanFacilityCommand command) {
        log.debug("Call and validate services for facility origination");

        CompletableFuture<Result<Void>> depositFuture =
                CompletableFuture.supplyAsync(() -> validateDeposit(command), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Void>> isDepositClosedFuture =
                CompletableFuture.supplyAsync(() -> isDepositClosed(command), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Void>> validateEconomicalSectorFuture =
                CompletableFuture.supplyAsync(() -> validateEconomicalSector(command), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Void>> validateEconomicalSectionForLoanTypeFuture =
                CompletableFuture.supplyAsync(() -> validateEconomicalSectionForLoanType(command), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Void>> validateDebtorDepositFuture =
                CompletableFuture.supplyAsync(() -> validateDebtorDeposit(command), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Void>> validateCreditorDepositFuture =
                CompletableFuture.supplyAsync(() -> validateCreditorDeposit(command), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Void>> validateSubSourceFuture =
                CompletableFuture.supplyAsync(() -> validateSubSource(command), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Void>> validateDepositCurrencyFuture =
                CompletableFuture.supplyAsync(() -> validateDepositCurrency(command), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<Void>> validateRequestReasonFuture =
                CompletableFuture.supplyAsync(() -> validateRequestReason(command), VIRTUAL_EXECUTOR);

        List<CompletableFuture<Result<Void>>> futures = List.of(
                depositFuture,
                isDepositClosedFuture,
                validateEconomicalSectorFuture,
                validateEconomicalSectionForLoanTypeFuture,
                validateDebtorDepositFuture,
                validateCreditorDepositFuture,
                validateSubSourceFuture,
                validateDepositCurrencyFuture,
                validateRequestReasonFuture);

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        Notification aggregatedNotification = Notification.create();
        futures.stream()
                .map(CompletableFuture::join)
                .forEach(result -> aggregatedNotification.merge(result.notification()));

        return aggregatedNotification.hasErrors() ? Result.failure(aggregatedNotification) : Result.success();
    }

    private Result<Void> validateDeposit(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        Result<DepositInfo> depositInfo = getDepositInfo(depositNumber);

        return depositInfo.isFailure() ? Result.failure(depositInfo.notification()) : Result.success();
    }

    private Result<Void> validateEconomicalSector(OriginateLoanFacilityCommand command) {
        String economicSectorCode = command.loanApplication().economicSector().code();
        Result<EconomicSector> economicSector = loadEconomicalSectorByCode(economicSectorCode);
        return economicSector.isFailure() ? Result.failure(economicSector.notification()) : Result.success();
    }

    private Result<Void> validateEconomicalSectionForLoanType(OriginateLoanFacilityCommand command) {
        String economicSectorCode = command.loanApplication().economicSector().code();
        Result<LoanTypeCode> loanTypeCodeResult = getLoanTypeCode(command.loanTypeId());
        if (loanTypeCodeResult.isFailure()) {
            return Result.failure(loanTypeCodeResult.notification());
        }
        Result<EconomicalSectorValidation> economicalSectorValidation =
                validateEconomicalSectionForLoanType(economicSectorCode, loanTypeCodeResult.value());

        return economicalSectorValidation.isFailure()
                ? Result.failure(economicalSectorValidation.notification())
                : !Objects.requireNonNull(economicalSectorValidation.value()).isValid()
                        ? Result.failure(Notification.create()
                                .addError(NotificationError.of(
                                        OriginateLoanFacilityErrorCodes.INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE,
                                        economicSectorCode,
                                        loanTypeCodeResult.value())))
                        : Result.success();
    }

    private Result<Void> isDepositClosed(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        String currencyCode = command.loanApplication().currency().value();
        Result<DepositClosedStatus> depositClosedStatus = isDepositClosed(depositNumber, currencyCode);
        return depositClosedStatus.isFailure()
                ? Result.failure(depositClosedStatus.notification())
                : Objects.requireNonNull(depositClosedStatus.value()).isClosed()
                        ? Result.failure(Notification.create()
                                .addError(NotificationError.of(
                                        OriginateLoanFacilityErrorCodes.DISBURSE_DESTINATION_DEPOSIT_IS_CLOSED,
                                        depositNumber)))
                        : Result.success();
    }

    private Result<Void> validateDebtorDeposit(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        String currencyCode = command.loanApplication().currency().value();
        Result<DebtorDepositValidation> validationResult = validateDebtorDeposit(depositNumber, currencyCode);

        return validationResult.isFailure()
                ? Result.failure(validationResult.notification())
                : !Objects.requireNonNull(validationResult.value()).isValidDebtorDeposit()
                        ? Result.failure(Notification.create()
                                .addError(NotificationError.of(
                                        OriginateLoanFacilityErrorCodes.INVALID_DEBTOR_DEPOSIT, depositNumber)))
                        : Result.success();
    }

    private Result<Void> validateCreditorDeposit(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        String currencyCode = command.loanApplication().currency().value();
        BigDecimal amount = command.loanApplication().requestedAmount().value();
        Result<CreditorDepositValidation> validationResult =
                validateCreditorDeposit(depositNumber, currencyCode, amount);

        return validationResult.isFailure()
                ? Result.failure(validationResult.notification())
                : !Objects.requireNonNull(validationResult.value()).isValidCreditorDeposit()
                        ? Result.failure(Notification.create()
                                .addError(NotificationError.of(
                                        OriginateLoanFacilityErrorCodes.INVALID_CREDITOR_DEPOSIT, depositNumber)))
                        : Result.success();
    }

    private Result<Void> validateSubSource(OriginateLoanFacilityCommand command) {
        String subSourceCode = Objects.requireNonNull(
                Objects.requireNonNull(command.loanApplication().subSource()).code());
        Result<SubSource> validationResult = loadResourceByCode(subSourceCode);

        return validationResult.isFailure() ? Result.failure(validationResult.notification()) : Result.success();
    }

    private Result<Void> validateRequestReason(OriginateLoanFacilityCommand command) {
        String requestReasonCode = Objects.requireNonNull(
                Objects.requireNonNull(command.loanApplication().requestReason())
                        .code());
        Result<ReasonType> validationResult = loadRequestReasonByCode(requestReasonCode);

        return validationResult.isFailure() ? Result.failure(validationResult.notification()) : Result.success();
    }

    private Result<Void> validateDepositCurrency(OriginateLoanFacilityCommand command) {
        String depositNumber = command.loanApplication().disburseDestination().depositNumber();
        String currencyCode = command.loanApplication().currency().value();
        Result<CurrencyValidation> validationResult = hasDepositAllowedCurrencies(depositNumber, currencyCode);

        return validationResult.isFailure()
                ? Result.failure(validationResult.notification())
                : !Objects.requireNonNull(validationResult.value()).isValid()
                        ? Result.failure(Notification.create()
                                .addError(NotificationError.of(
                                        OriginateLoanFacilityErrorCodes.INVALID_DEPOSIT_CURRENCY, depositNumber)))
                        : Result.success();
    }

    private Result<PartyInfo> loadCustomerInfo(String customerNumber) {
        return customerServicePort.loadCustomerInfo(customerNumber, CustomerInfoLoadOptions.allIncluded());
    }

    private Result<DepositInfo> getDepositInfo(String depositNumber) {
        return depositServicePort.getDepositInfo(
                DepositNumber.valueOf(depositNumber).getValue());
    }

    private Result<CurrencyValidation> hasDepositAllowedCurrencies(String depositNumber, String currencyCode) {
        return depositServicePort.hasDepositAllowedCurrencies(
                DepositNumber.valueOf(depositNumber).getValue(),
                Collections.singletonList(CurrencyType.valueOf(currencyCode).value()));
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

    private Result<EconomicalSectorValidation> validateEconomicalSectionForLoanType(
            String economicSectorCode, LoanTypeCode loanTypeCode) {
        return loanServicePort.validateEconomicalSectorForLoanType(
                EconomicSector.of(economicSectorCode).getValue(), loanTypeCode);
    }

    private Result<LoanTypeCode> getLoanTypeCode(UUID loanTypeId) {
        Optional<String> loanTypeCode = tradeLoanTypeRepository.getLoanTypeCode(LoanTypeId.of(loanTypeId));
        return loanTypeCode.isEmpty()
                ? Result.failure(Notification.create()
                        .addError(NotificationError.of(OriginateLoanFacilityErrorCodes.INVALID_LOAN_TYPE, loanTypeId)))
                : LoanTypeCode.of(loanTypeCode.get());
    }

    // TODO reasonType service must change load-reason-type

    // TODO must be feature
    private Result<Void> validateMainCustomer(OriginateLoanFacilityCommand command) {
        CompletableFuture<Result<PartyInfo>> mainCustomerFuture = CompletableFuture.supplyAsync(
                () -> loadCustomerInfo(command.loanApplication().customer().customerNumber()), VIRTUAL_EXECUTOR);

        Result<PartyInfo> mainCustomer = mainCustomerFuture.join();
        return mainCustomer.isFailure() ? Result.failure(mainCustomer.notification()) : Result.success();
    }

    private Result<Void> validateGuarantors(OriginateLoanFacilityCommand command) {
        List<CompletableFuture<Result<PartyInfo>>> guarantorFutures = command.loanApplication().guarantors().stream()
                .map(guarantor -> CompletableFuture.supplyAsync(
                        () -> loadCustomerInfo(guarantor.customerNumber()), VIRTUAL_EXECUTOR))
                .toList();

        Result<List<PartyInfo>> guarantorsResult = collectGuarantors(guarantorFutures);
        return guarantorsResult.isFailure() ? Result.failure(guarantorsResult.notification()) : Result.success();
    }

    private Result<List<PartyInfo>> collectGuarantors(List<CompletableFuture<Result<PartyInfo>>> guarantorFutures) {
        List<PartyInfo> guarantors = new ArrayList<>();
        Notification aggregatedNotification = Notification.create();

        for (CompletableFuture<Result<PartyInfo>> future : guarantorFutures) {
            Result<PartyInfo> result = future.join();
            aggregatedNotification.merge(result.notification());
            if (result.hasValue()) {
                guarantors.add(result.value());
            }
        }

        return aggregatedNotification.hasErrors()
                ? Result.failure(aggregatedNotification)
                : Result.of(guarantors, aggregatedNotification);
    }
}
