package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class HeartbeatKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String probeId;
    private long publishedAtEpochMs;
    private long respondedAtEpochMs;
    private @Nullable String consumerNode;

    public @Nullable String getProbeId() {
        return probeId;
    }

    public void setProbeId(@Nullable String probeId) {
        this.probeId = probeId;
    }

    public long getPublishedAtEpochMs() {
        return publishedAtEpochMs;
    }

    public void setPublishedAtEpochMs(long publishedAtEpochMs) {
        this.publishedAtEpochMs = publishedAtEpochMs;
    }

    public long getRespondedAtEpochMs() {
        return respondedAtEpochMs;
    }

    public void setRespondedAtEpochMs(long respondedAtEpochMs) {
        this.respondedAtEpochMs = respondedAtEpochMs;
    }

    public @Nullable String getConsumerNode() {
        return consumerNode;
    }

    public void setConsumerNode(@Nullable String consumerNode) {
        this.consumerNode = consumerNode;
    }
}
