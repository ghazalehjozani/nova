package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CreditorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CurrencyValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DebtorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DepositClosedStatus;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DepositValidationRules {

    private final DepositServicePort depositServicePort;

    public Result<Unit> validateDeposit(OriginateLoanFacilityCommand command) {
        DisburseDestinationDto disburseDestination = command.loanApplication().disburseDestination();
        return switch (disburseDestination) {
            case DisburseDestinationDto.AccountDestinationDto(var accountNumber) -> Result.success();
            case DisburseDestinationDto.DepositDestinationDto(var depositNumber) -> {
                Result<DepositInfo> result = getDepositInfo(depositNumber);
                if (result.isFailure()) {
                    yield Result.failure(result.err().orElseThrow());
                }
                yield Result.success();
            }
        };
    }

    public Result<Unit> isDepositClosed(OriginateLoanFacilityCommand command) {
        DisburseDestinationDto destination = command.loanApplication().disburseDestination();

        return switch (destination) {
            case DisburseDestinationDto.DepositDestinationDto(var depositNumber) -> {
                String currency = command.loanApplication().currency().value();
                Result<DepositClosedStatus> result = isDepositClosed(depositNumber, currency);

                yield validateBusinessRule(
                        result,
                        status -> !status.isClosed(),
                        () -> Notification.ofError(
                                OriginateLoanFacilityErrorCodes.DISBURSE_DESTINATION_DEPOSIT_IS_CLOSED, depositNumber));
            }
            case DisburseDestinationDto.AccountDestinationDto(var accountNumber) -> Result.success();
        };
    }

    public Result<Unit> validateDebtorDeposit(OriginateLoanFacilityCommand command) {
        DisburseDestinationDto destination = command.loanApplication().disburseDestination();

        return switch (destination) {
            case DisburseDestinationDto.DepositDestinationDto(var depositNumber) -> {
                String currency = command.loanApplication().currency().value();
                Result<DebtorDepositValidation> result = validateDebtorDeposit(depositNumber, currency);

                yield validateBusinessRule(
                        result,
                        DebtorDepositValidation::isValidDebtorDeposit,
                        () -> Notification.ofError(
                                OriginateLoanFacilityErrorCodes.INVALID_DEBTOR_DEPOSIT, depositNumber));
            }
            case DisburseDestinationDto.AccountDestinationDto(var accountNumber) -> Result.success();
        };
    }

    public Result<Unit> validateCreditorDeposit(OriginateLoanFacilityCommand command) {
        DisburseDestinationDto destination = command.loanApplication().disburseDestination();

        return switch (destination) {
            case DisburseDestinationDto.DepositDestinationDto(var depositNumber) -> {
                String currency = command.loanApplication().currency().value();
                BigDecimal amount = command.loanApplication().requestedAmount().value();
                Result<CreditorDepositValidation> result = validateCreditorDeposit(depositNumber, currency, amount);

                yield validateBusinessRule(
                        result,
                        CreditorDepositValidation::isValidCreditorDeposit,
                        () -> Notification.ofError(
                                OriginateLoanFacilityErrorCodes.INVALID_CREDITOR_DEPOSIT, depositNumber));
            }
            case DisburseDestinationDto.AccountDestinationDto(var accountNumber) -> Result.success();
        };
    }

    public Result<Unit> validateDepositCurrency(OriginateLoanFacilityCommand command) {
        DisburseDestinationDto destination = command.loanApplication().disburseDestination();

        return switch (destination) {
            case DisburseDestinationDto.DepositDestinationDto(var depositNumber) -> {
                String currency = command.loanApplication().currency().value();
                Result<CurrencyValidation> result = hasDepositAllowedCurrencies(depositNumber, currency);

                yield validateBusinessRule(
                        result,
                        CurrencyValidation::isValid,
                        () -> Notification.ofError(
                                OriginateLoanFacilityErrorCodes.INVALID_DEPOSIT_CURRENCY, depositNumber));
            }
            case DisburseDestinationDto.AccountDestinationDto(var accountNumber) -> Result.success();
        };
    }

    private Result<DepositInfo> getDepositInfo(String depositNumber) {
        return depositServicePort.getDepositInfo(
                DepositNumber.valueOf(depositNumber).unwrap());
    }

    private Result<CurrencyValidation> hasDepositAllowedCurrencies(String depositNumber, String currencyCode) {
        return depositServicePort.hasDepositAllowedCurrencies(
                DepositNumber.valueOf(depositNumber).unwrap(),
                List.of(Objects.requireNonNull(
                        CurrencyType.valueOf(currencyCode).unwrap())));
    }

    private Result<DebtorDepositValidation> validateDebtorDeposit(String depositNumber, String currencyCode) {
        return depositServicePort.validateDebtorDeposit(
                DepositNumber.valueOf(depositNumber).unwrap(),
                CurrencyType.valueOf(currencyCode).unwrap());
    }

    private Result<CreditorDepositValidation> validateCreditorDeposit(
            String depositNumber, String currencyCode, BigDecimal amount) {
        return depositServicePort.validateCreditorDeposit(
                DepositNumber.valueOf(depositNumber).unwrap(),
                CurrencyType.valueOf(currencyCode).unwrap(),
                amount);
    }

    private Result<DepositClosedStatus> isDepositClosed(String depositNumber, String currencyCode) {
        return depositServicePort.isDepositClosed(
                DepositNumber.valueOf(depositNumber).unwrap(),
                CurrencyType.valueOf(currencyCode).unwrap());
    }

    private <T> Result<Unit> validateBusinessRule(
            Result<T> result, Predicate<T> isValid, Supplier<Notification> errorSupplier) {

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        if (!isValid.test(result.unwrap())) {
            return Result.failure(errorSupplier.get());
        }

        return Result.success();
    }
}
