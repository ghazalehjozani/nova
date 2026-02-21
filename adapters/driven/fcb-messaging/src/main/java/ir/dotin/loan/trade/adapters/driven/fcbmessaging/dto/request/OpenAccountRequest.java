package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.math.BigDecimal;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class OpenAccountRequest extends FcbKafkaBaseRequest {

    private final String transactionId;
    private final String topic;
    private final String swiftCode;
    private final String branchCode;
    private final boolean createAccountGroup;
    private final String newAccBranchCode;
    private final String newAccAccountNumber;
    private final String newAccTitle;
    private final BigDecimal newAccAmount;
    private final BigDecimal newAccMinAmount;
    private final BigDecimal newAccMaxAmount;
    private final BigDecimal newAccBaseCurrencyAmount;
    private final BigDecimal newAccDebtorAmount;
    private final BigDecimal newAccCreditorAmount;

    public OpenAccountRequest(
            String transactionId,
            String topic,
            String swiftCode,
            String branchCode,
            boolean createAccountGroup,
            String newAccBranchCode,
            String newAccAccountNumber,
            String newAccTitle,
            BigDecimal newAccAmount,
            BigDecimal newAccMinAmount,
            BigDecimal newAccMaxAmount,
            BigDecimal newAccBaseCurrencyAmount,
            BigDecimal newAccDebtorAmount,
            BigDecimal newAccCreditorAmount) {
        super("nova-open-account");
        this.transactionId = transactionId;
        this.topic = topic;
        this.swiftCode = swiftCode;
        this.branchCode = branchCode;
        this.createAccountGroup = createAccountGroup;
        this.newAccBranchCode = newAccBranchCode;
        this.newAccAccountNumber = newAccAccountNumber;
        this.newAccTitle = newAccTitle;
        this.newAccAmount = newAccAmount;
        this.newAccMinAmount = newAccMinAmount;
        this.newAccMaxAmount = newAccMaxAmount;
        this.newAccBaseCurrencyAmount = newAccBaseCurrencyAmount;
        this.newAccDebtorAmount = newAccDebtorAmount;
        this.newAccCreditorAmount = newAccCreditorAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTopic() {
        return topic;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public boolean isCreateAccountGroup() {
        return createAccountGroup;
    }

    public String getNewAccBranchCode() {
        return newAccBranchCode;
    }

    public String getNewAccAccountNumber() {
        return newAccAccountNumber;
    }

    public String getNewAccTitle() {
        return newAccTitle;
    }

    public BigDecimal getNewAccAmount() {
        return newAccAmount;
    }

    public BigDecimal getNewAccMinAmount() {
        return newAccMinAmount;
    }

    public BigDecimal getNewAccMaxAmount() {
        return newAccMaxAmount;
    }

    public BigDecimal getNewAccBaseCurrencyAmount() {
        return newAccBaseCurrencyAmount;
    }

    public BigDecimal getNewAccDebtorAmount() {
        return newAccDebtorAmount;
    }

    public BigDecimal getNewAccCreditorAmount() {
        return newAccCreditorAmount;
    }
}
