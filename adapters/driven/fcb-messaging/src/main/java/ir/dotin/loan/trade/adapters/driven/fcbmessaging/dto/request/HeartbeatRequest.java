package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

/**
 * Synthetic probe payload. Produced by the publisher-side health probe and consumed by the integration service's
 * {@code HeartbeatHandler}, which simply echoes it back with a timestamp.
 *
 * <p>Operation name {@code "heartbeat"} must match {@code FcbHealthProperties.heartbeatOperationName} and the
 * consumer-side {@code NovaIntegrationOperationType.HEARTBEAT}.
 */
public final class HeartbeatRequest extends FcbKafkaBaseRequest {

    private final String probeId;
    private final long publishedAtEpochMs;

    public HeartbeatRequest(String probeId, long publishedAtEpochMs) {
        super("heartbeat");
        this.probeId = probeId;
        this.publishedAtEpochMs = publishedAtEpochMs;
    }

    public String getProbeId() {
        return probeId;
    }

    public long getPublishedAtEpochMs() {
        return publishedAtEpochMs;
    }
}
