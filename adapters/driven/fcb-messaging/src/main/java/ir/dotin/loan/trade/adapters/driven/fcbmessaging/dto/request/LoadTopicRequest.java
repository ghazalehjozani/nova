package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class LoadTopicRequest extends FcbKafkaBaseRequest {

    private final List<String> topicCodes;

    public LoadTopicRequest(List<String> topicCodes) {
        super("load-topic-by-code");
        this.topicCodes = topicCodes;
    }

    public List<String> getTopicCodes() {
        return topicCodes;
    }
}
