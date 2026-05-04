package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class HeartbeatKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String probeId;
    private long publishedAtEpochMs;
    private long respondedAtEpochMs;
    private @Nullable String consumerNode;

    private @Nullable String healthStatus;
    private int totalComponents;
    private int upComponents;
    private int degradedComponents;
    private int downComponents;
    private long lastHealthChangeAtEpochMs;

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

    public @Nullable String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(@Nullable String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public int getTotalComponents() {
        return totalComponents;
    }

    public void setTotalComponents(int totalComponents) {
        this.totalComponents = totalComponents;
    }

    public int getUpComponents() {
        return upComponents;
    }

    public void setUpComponents(int upComponents) {
        this.upComponents = upComponents;
    }

    public int getDegradedComponents() {
        return degradedComponents;
    }

    public void setDegradedComponents(int degradedComponents) {
        this.degradedComponents = degradedComponents;
    }

    public int getDownComponents() {
        return downComponents;
    }

    public void setDownComponents(int downComponents) {
        this.downComponents = downComponents;
    }

    public long getLastHealthChangeAtEpochMs() {
        return lastHealthChangeAtEpochMs;
    }

    public void setLastHealthChangeAtEpochMs(long lastHealthChangeAtEpochMs) {
        this.lastHealthChangeAtEpochMs = lastHealthChangeAtEpochMs;
    }
}
