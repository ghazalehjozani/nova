package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Synthetic probe payload. Produced by the publisher-side health probe and consumed by the integration service's
 * {@code HeartbeatHandler}, which simply echoes it back with a timestamp.
 *
 * <p>Operation name {@code "heartbeat"} must match {@code FcbHealthProperties.heartbeatOperationName} and the
 * consumer-side {@code NovaIntegrationOperationType.HEARTBEAT}.
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public final class HeartbeatRequest extends FcbKafkaBaseRequest {

    @Builder.Default
    private String operationName = "heartbeat";

    private final String probeId;
    private final long publishedAtEpochMs;
}
