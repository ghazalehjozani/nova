package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class OpenAccountByTopicRequest extends FcbKafkaBaseRequest {

    private final String title;
    private final String topicCode;
    private final String branchCode;

    public OpenAccountByTopicRequest(String title, String topicCode, String branchCode) {
        super("electronic-bill-create-account");
        this.title = title;
        this.topicCode = topicCode;
        this.branchCode = branchCode;
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
}
