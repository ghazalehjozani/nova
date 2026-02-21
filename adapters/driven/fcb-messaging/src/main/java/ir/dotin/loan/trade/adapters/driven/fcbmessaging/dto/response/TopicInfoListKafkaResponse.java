package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import java.util.List;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class TopicInfoListKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable List<TopicInfoDto> topics;

    public @Nullable List<TopicInfoDto> getTopics() {
        return topics;
    }

    public void setTopics(@Nullable List<TopicInfoDto> topics) {
        this.topics = topics;
    }
}
