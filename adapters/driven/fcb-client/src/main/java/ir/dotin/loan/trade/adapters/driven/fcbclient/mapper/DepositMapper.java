package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;
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

        Result<CurrencyType> currencyResult = Result.success(null);

        if (response.getCurrency() != null && response.getCurrency().getCode() != null) {
            currencyResult = CurrencyType.valueOf(response.getCurrency().getCode());
            if (currencyResult.isFailure()) {
                notification.merge(currencyResult.notification());
            }
        } else {
            log.warn("Currency information is null for deposit: {}", response.getNumber());
        }

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
        Notification notification = Notification.create();

        if (fcbResponse.getCurrencySwiftCode() == null
                || fcbResponse.getCurrencySwiftCode().isBlank()) {
            log.error("FCB response missing currency swift code");
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Currency swift code is missing in response");
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        Boolean isClosedValue = fcbResponse.getIsClosed();
        if (isClosedValue == null) {
            log.warn("FCB response has null isClosed value, defaulting to false");
            isClosedValue = false;
        }

        DepositClosedStatus status = new DepositClosedStatus(isClosedValue, fcbResponse.getCurrencySwiftCode());

        return Result.success(status);
    }

    public Result<DebtorCreditorDepositValidation> mapToDomainDebtorDepositValidation(
            ValidateDebtorDepositResponse fcbResponse) {
        Boolean isDebtorValue = fcbResponse.getIsDebtor();
        if (isDebtorValue == null) {
            log.warn("FCB response has null isDebtor value, defaulting to false");
        }

        DebtorCreditorDepositValidation validation = new DebtorCreditorDepositValidation(Direction.DEBIT);
        return Result.success(validation);
    }

    public Result<DebtorCreditorDepositValidation> mapToDomainCreditorDepositValidation(
            ValidateCreditorDepositResponse fcbResponse) {
        Boolean isCreditorValue = fcbResponse.getIsCreditor();
        if (isCreditorValue == null) {
            log.warn("FCB response has null isCreditor value, defaulting to false");
        }

        DebtorCreditorDepositValidation validation = new DebtorCreditorDepositValidation(Direction.CREDIT);
        return Result.success(validation);
    }
}
