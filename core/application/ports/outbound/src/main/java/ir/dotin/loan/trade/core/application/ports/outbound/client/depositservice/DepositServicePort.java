package ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice;

import java.math.BigDecimal;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.feignclient.DebtorCreditorDepositValidation;
import ir.dotin.loan.baseloan.core.domain.shared.vo.feignclient.DepositClosedStatus;

public interface DepositServicePort {

    Result<DepositInfo> getDepositInfo(DepositNumber depositNumber);

    Result<DepositClosedStatus> isDepositClosed(DepositNumber depositNumber, CurrencyType currencyType);

    Result<DebtorCreditorDepositValidation> validateDebtorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType);

    Result<DebtorCreditorDepositValidation> validateCreditorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType, BigDecimal amount);
}
