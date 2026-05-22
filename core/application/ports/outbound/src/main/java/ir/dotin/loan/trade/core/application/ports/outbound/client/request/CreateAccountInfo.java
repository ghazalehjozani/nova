package ir.dotin.loan.trade.core.application.ports.outbound.client.request;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.commons.domain.vo.ValueObject;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;

public record CreateAccountInfo(
        @NonNull String transactionId,
        @NonNull String topicCode,
        @NonNull CurrencyType currencyType,
        @NonNull BranchCode branchCode,
        @NonNull Boolean createAccountGroup,
        AccountId newAccAccountId,
        BranchCode newAccBranchCode,
        Title newAccTitle,
        Money newAccAmount,
        Money newAccMinAmount,
        Money newAccMaxAmount,
        Money newAccBaseCurrencyAmount,
        Money newAccDebtorAmount,
        Money newAccCreditorAmount)
        implements ValueObject<CreateAccountInfo> {

    public static Result<CreateAccountInfo> of(
            String transactionId,
            String topic,
            CurrencyType currencyType,
            BranchCode branchCode,
            Boolean createAccountGroup,
            AccountId newAccAccountId,
            BranchCode newAccBranchCode,
            Title newAccTitle,
            Money newAccAmount,
            Money newAccMinAmount,
            Money newAccMaxAmount,
            Money newAccBaseCurrencyAmount,
            Money newAccDebtorAmount,
            Money newAccCreditorAmount) {

        CreateAccountInfo createAccountInfo = new CreateAccountInfo(
                transactionId,
                topic,
                currencyType,
                branchCode,
                createAccountGroup,
                newAccAccountId,
                newAccBranchCode,
                newAccTitle,
                newAccAmount,
                newAccMinAmount,
                newAccMaxAmount,
                newAccBaseCurrencyAmount,
                newAccDebtorAmount,
                newAccCreditorAmount);

        return Result.success(createAccountInfo);
    }
}
