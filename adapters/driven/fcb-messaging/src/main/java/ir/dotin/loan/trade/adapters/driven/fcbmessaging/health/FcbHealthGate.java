package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.RequiredArgsConstructor;

/**
 * Gate consulted by {@code FcbKafkaClient} before every request.
 *
 * <p>In the UP / DEGRADED / RECOVERING states the gate is transparent. In the DOWN state it fast-fails without touching
 * Kafka so broken downstreams don't cascade into thread-pool exhaustion. In UNKNOWN it delegates to
 * {@code fail-open-on-unknown} config.
 *
 * <p>Crucially, user requests are never used to probe recovery — that is the sole responsibility of
 * {@link FcbHealthProbe}.
 */
@Component
@RequiredArgsConstructor
public class FcbHealthGate {

    private final FcbHealthState state;
    private final FcbHealthProperties properties;

    /**
     * @return {@code null} if the call is permitted, otherwise a pre-built failure Result to return to the caller
     *     without dispatching to Kafka.
     */
    public Result<FcbKafkaBaseResponse> checkPermitted(String operationName) {
        FcbHealthSnapshot snap = state.snapshot();
        return switch (snap.status()) {
            case UP, DEGRADED, RECOVERING -> null;
            case UNKNOWN -> properties.failOpenOnUnknown() ? null : rejectUnknown(operationName);
            case DOWN -> rejectDown(snap, operationName);
        };
    }

    private Result<FcbKafkaBaseResponse> rejectDown(FcbHealthSnapshot snap, String operationName) {
        String reason = snap.lastError() == null ? "unknown" : snap.lastError();
        return Result.failure(Notification.ofError(
                CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE,
                "FCB Kafka gate CLOSED (DOWN): operation=" + operationName + ", cause=" + reason));
    }

    private Result<FcbKafkaBaseResponse> rejectUnknown(String operationName) {
        return Result.failure(Notification.ofError(
                CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE,
                "FCB Kafka gate CLOSED (health unknown, waiting for first probe): operation=" + operationName));
    }
}
