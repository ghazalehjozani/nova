package ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice;

import java.math.BigDecimal;
import java.util.List;

import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CreditorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CurrencyValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DebtorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DepositClosedStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;

public interface DepositServicePort {

    Result<DepositInfo> getDepositInfo(DepositNumber depositNumber);

    Result<DepositClosedStatus> isDepositClosed(DepositNumber depositNumber, CurrencyType currencyType);

    Result<DebtorDepositValidation> validateDebtorDeposit(DepositNumber depositNumber, CurrencyType currencyType);

    Result<CreditorDepositValidation> validateCreditorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType, BigDecimal amount);

    Result<CurrencyValidation> hasDepositAllowedCurrencies(
            DepositNumber depositNumber, List<CurrencyType> currencyTypes);

    Result<List<PartyInfoResponse>> getAllDepositSignerOwnerCustomer(String depositNumber);
}
