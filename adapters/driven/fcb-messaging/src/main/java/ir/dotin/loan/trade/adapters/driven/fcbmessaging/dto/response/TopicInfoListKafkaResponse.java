package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class TopicInfoListKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable List<TopicInfoDto> topics;

    public @Nullable List<TopicInfoDto> getTopics() {
        return topics;
    }

    public void setTopics(@Nullable List<TopicInfoDto> topics) {
        this.topics = topics;
    }
}
