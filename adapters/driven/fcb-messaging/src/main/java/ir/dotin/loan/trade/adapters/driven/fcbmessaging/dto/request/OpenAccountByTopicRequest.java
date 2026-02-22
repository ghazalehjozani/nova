package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class OpenAccountByTopicRequest extends FcbKafkaBaseRequest {

    private final String title;
    private final String topicCode;
    private final String branchCode;
    private final String currencyCode;
    private final String transactionId;

    public OpenAccountByTopicRequest(String title, String topicCode, String branchCode, String currencyCode, String transactionId) {
        super("electronic-bill-create-account");
        this.title = title;
        this.topicCode = topicCode;
        this.branchCode = branchCode;
        this.currencyCode = currencyCode;
        this.transactionId = transactionId;
    }

    public String getTitle() {
        return title;
    }

    public String getTopicCode() {
        return topicCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
