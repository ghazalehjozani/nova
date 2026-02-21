package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class FindOrCreateAccountRequest extends FcbKafkaBaseRequest {

    private final String title;
    private final String topicCode;

    public FindOrCreateAccountRequest(String title, String topicCode) {
        super("find-or-create-account");
        this.title = title;
        this.topicCode = topicCode;
    }

    public String getTitle() {
        return title;
    }

    public String getTopicCode() {
        return topicCode;
    }
}
