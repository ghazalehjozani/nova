package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.math.BigDecimal;
import java.util.*;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.EconomicSectorDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SamatDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;
import ir.dotin.loan.trade.core.application.ports.outbound.client.samat.ValidateSamatPort;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityValidator {

    private final DepositServicePort depositServicePort;
    private final LoanServicePort loanServicePort;
    private final AccountServicePort accountServicePort;
    private final ValidateSamatPort validateSamatPort;

    @WithSpan("facility.validate.fanout")
    public Result<Unit> callAndValidateServices(OriginateLoanFacilityCommand command) {
        log.debug("Call and validate services for facility origination");

        List<Supplier<Result<Unit>>> tasks = List.of(
                () -> validateDeposit(command),
                () -> validateAccountNumber(command),
                () -> isDepositClosed(command),
                () -> validateEconomicalSector(command),
                () -> validateEconomicalSectionForLoanType(command),
                () -> validateDebtorDeposit(command),
                () -> validateCreditorDeposit(command),
                () -> validateSubSource(command),
                () -> validateDepositCurrency(command),
                () -> validateSamat(command),
                () -> validateRequestReason(command));

        return ParallelFanout.allVoid(tasks);
    }

    private Result<Unit> validateDeposit(OriginateLoanFacilityCommand command) {
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

    private Result<Unit> validateSamat(OriginateLoanFacilityCommand command) {
        SamatDto samatDto = command.loanApplication().samat();
        EconomicSectorDto economicSectorDto = command.loanApplication().economicSector();
        Samat samat = samatDtoToSamat(samatDto);
        if (samat == null) {
            // samatDto was null — nothing to validate
            return Result.success();
        }
        return validateSamatPort.validateSamat(samat, command.loanTypeCode(), economicSectorDto.code());
    }

    private Result<Unit> validateEconomicalSector(OriginateLoanFacilityCommand command) {
        String code = command.loanApplication().economicSector().code();

        return loadEconomicalSectorByCode(code).flatMap(this::validateNotParent);
    }

    private Result<Unit> validateNotParent(EconomicalSectorResponse sector) {
        if (Boolean.TRUE.equals(sector.hasChild())) {
            return Result.failure(
                    Notification.ofError(OriginateLoanFacilityErrorCodes.ECONOMIC_SECTOR_IS_PARENT, sector.code()));
        }
        return Result.success();
    }

    private Result<Unit> validateEconomicalSectionForLoanType(OriginateLoanFacilityCommand command) {
        String sectorCode = command.loanApplication().economicSector().code();
        String typeCodeRaw = command.loanTypeCode();
        LoanTypeCode loanTypeCode = LoanTypeCode.of(typeCodeRaw).unwrap();

        Result<EconomicalSectorValidation> result = validateEconomicalSectorForLoanType(sectorCode, loanTypeCode);

        return validateBusinessRule(
                result,
                EconomicalSectorValidation::isValid,
                () -> Notification.ofError(
                        OriginateLoanFacilityErrorCodes.INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE,
                        sectorCode,
                        loanTypeCode));
    }

    private Result<Unit> isDepositClosed(OriginateLoanFacilityCommand command) {
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

    private Result<Unit> validateDebtorDeposit(OriginateLoanFacilityCommand command) {
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

    private Result<Unit> validateCreditorDeposit(OriginateLoanFacilityCommand command) {
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

    private Result<Unit> validateDepositCurrency(OriginateLoanFacilityCommand command) {
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

    private Result<Unit> validateSubSource(OriginateLoanFacilityCommand command) {
        var subSource = command.loanApplication().subSource();
        if (subSource == null) {
            // subSource is optional; skip validation when not provided
            return Result.success();
        }
        String code = subSource.code();
        Result<SubSource> result = loadResourceByCode(code);

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        return Result.success();
    }

    private Result<Unit> validateRequestReason(OriginateLoanFacilityCommand command) {
        String code = command.loanApplication().requestReason().code();
        Result<ReasonType> result = loadRequestReasonByCode(code);

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        return Result.success();
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

    private Result<SubSource> loadResourceByCode(String subSourceCode) {
        return loanServicePort.loadResourceByCode(subSourceCode);
    }

    private Result<ReasonType> loadRequestReasonByCode(String requestReasonCode) {
        return loanServicePort.loadReasonTypeForCreate(requestReasonCode);
    }

    private Result<DepositClosedStatus> isDepositClosed(String depositNumber, String currencyCode) {
        return depositServicePort.isDepositClosed(
                DepositNumber.valueOf(depositNumber).unwrap(),
                CurrencyType.valueOf(currencyCode).unwrap());
    }

    private Result<EconomicalSectorResponse> loadEconomicalSectorByCode(String economicSectorCode) {
        return loanServicePort.loadEconomicalSector(
                EconomicSector.of(economicSectorCode).unwrap());
    }

    private Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
            String economicSectorCode, LoanTypeCode loanTypeCode) {
        return loanServicePort.validateEconomicalSectorForLoanType(
                EconomicSector.of(economicSectorCode).unwrap(), loanTypeCode);
    }

    private Result<Unit> validateAccountNumber(OriginateLoanFacilityCommand command) {
        DisburseDestinationDto disburseDestination = command.loanApplication().disburseDestination();

        return switch (disburseDestination) {
            case DisburseDestinationDto.AccountDestinationDto(var accountNumber) -> {
                Result<AccountNumber> result = accountServicePort.validateAccountNumber(accountNumber);

                if (result.isFailure()) {
                    yield Result.failure(OriginateLoanFacilityErrorCodes.INVALID_ACCOUNT_NUMBER, accountNumber);
                }
                yield Result.success();
            }
            case DisburseDestinationDto.DepositDestinationDto(var ignored) -> Result.success();
        };
    }

    protected @Nullable Samat samatDtoToSamat(@Nullable SamatDto samatDto) {
        if (samatDto == null) {
            return null;
        }

        String trackingNumber = null;
        String isicEconomicSector = null;
        String subIsicEconomicSector = null;
        String useType = null;
        String exceptionCode = null;
        String consumptionPlaceCode = null;

        if (samatDto.trackingNumber() != null) {
            trackingNumber = samatDto.trackingNumber();
        }
        if (samatDto.isicEconomicSector() != null) {
            isicEconomicSector = samatDto.isicEconomicSector();
        }
        if (samatDto.subIsicEconomicSector() != null) {
            subIsicEconomicSector = samatDto.subIsicEconomicSector();
        }
        if (samatDto.useType() != null) {
            useType = samatDto.useType();
        }
        if (samatDto.exceptionCode() != null) {
            exceptionCode = samatDto.exceptionCode();
        }
        if (samatDto.consumptionPlaceCode() != null) {
            consumptionPlaceCode = samatDto.consumptionPlaceCode();
        }

        // Samat fields are @NonNull; @Nullable samatDto sub-fields are mapped to empty string when absent
        // (Samat.validate() will catch blank trackingNumber with a proper error result)
        Samat samat = new Samat(
                Objects.requireNonNullElse(trackingNumber, ""),
                Objects.requireNonNullElse(isicEconomicSector, ""),
                Objects.requireNonNullElse(subIsicEconomicSector, ""),
                Objects.requireNonNullElse(useType, ""),
                Objects.requireNonNullElse(exceptionCode, ""),
                Objects.requireNonNullElse(consumptionPlaceCode, ""));

        return samat;
    }
    // TODO reasonType service must change load-reason-type
}
