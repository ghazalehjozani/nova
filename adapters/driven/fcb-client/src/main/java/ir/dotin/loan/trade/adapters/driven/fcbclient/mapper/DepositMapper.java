package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositClosedResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ValidateCreditorDepositResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ValidateDebtorDepositResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DebtorCreditorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DepositClosedStatus;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class DepositMapper {

    public Result<DepositInfo> mapToDepositInfo(DepositInfoResponse response) {
        Notification notification = Notification.create();

        Result<DepositNumber> numberResult = DepositNumber.valueOf(response.getNumber());
        if (numberResult.isFailure()) notification.merge(numberResult.notification());

        Result<CurrencyType> currencyResult =
                CurrencyType.valueOf(response.getCurrency().getCode());
        if (currencyResult.isFailure()) notification.merge(currencyResult.notification());

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }
        return DepositInfo.of(
                numberResult.getValue(),
                response.getTitle(),
                response.getType(),
                currencyResult.getValue(),
                String.valueOf(response.getStatus()),
                response.getIsExternalDeposit(),
                response.getBranchCode(),
                response.getOwnerNationalCodes());
    }

    public Result<DepositClosedStatus> mapToDomainDepositClosedStatus(DepositClosedResponse fcbResponse) {

        try {
            Boolean isClosedValue = fcbResponse.getIsClosed();
            if (isClosedValue == null) {
                log.warn("FCB response has null isClosed value, defaulting to false");
                isClosedValue = false;
            }

            Result<DepositClosedStatus> result =
                    DepositClosedStatus.of(isClosedValue, fcbResponse.getCurrencySwiftCode());

            if (result.isFailure()) {
                log.error(
                        "Failed to create DepositClosedStatus: {}",
                        result.notification().getErrorMessages());
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain DepositClosedStatus", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse deposit closed status: " + e.getMessage()));
        }
    }

    public Result<DebtorCreditorDepositValidation> mapToDomainDebtorDepositValidation(
            ValidateDebtorDepositResponse fcbResponse) {

        try {
            Boolean isDebtorValue = fcbResponse.getIsDebtor();
            if (isDebtorValue == null) {
                log.warn("FCB response has null isDebtor value, defaulting to false");
                isDebtorValue = false;
            }

            Result<DebtorCreditorDepositValidation> result = DebtorCreditorDepositValidation.of(isDebtorValue);

            if (result.isFailure()) {
                log.error(
                        "Failed to create DebtorDepositValidation: {}",
                        result.notification().getErrorMessages());
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain DebtorDepositValidation", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse debtor deposit validation: " + e.getMessage()));
        }
    }

    public Result<DebtorCreditorDepositValidation> mapToDomainCreditorDepositValidation(
            ValidateCreditorDepositResponse fcbResponse) {

        try {
            // Handle null isCreditor
            Boolean isCreditorValue = fcbResponse.getIsCreditor();
            if (isCreditorValue == null) {
                log.warn("FCB response has null isCreditor value, defaulting to false");
                isCreditorValue = false;
            }

            Result<DebtorCreditorDepositValidation> result = isCreditorValue
                    ? DebtorCreditorDepositValidation.notDebtor()
                    : DebtorCreditorDepositValidation.debtor();

            if (result.isFailure()) {
                log.error(
                        "Failed to create CreditorDepositValidation: {}",
                        result.notification().getErrorMessages());
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain CreditorDepositValidation", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse creditor deposit validation: " + e.getMessage()));
        }
    }
}
