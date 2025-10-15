package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositInfoResponse;

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
}
