package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FcbHealthGate {

    private final FcbHealthState state;
    private final FcbHealthProperties properties;

    public Result<Void> checkPermitted(String operationName) {
        FcbHealthSnapshot snap = state.snapshot();
        return switch (snap.status()) {
            case UP, DEGRADED, RECOVERING -> Result.success();
            case UNKNOWN -> properties.failOpenOnUnknown() ? Result.success() : rejectUnknown(operationName);
            case DOWN -> rejectDown(snap, operationName);
        };
    }

    private Result<Void> rejectDown(FcbHealthSnapshot snap, String operationName) {
        String reason = snap.lastError() == null ? "unknown" : snap.lastError();
        return Result.failure(Notification.ofError(
                CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE,
                "FCB Kafka gate CLOSED (DOWN): operation=" + operationName + ", cause=" + reason));
    }

    private Result<Void> rejectUnknown(String operationName) {
        return Result.failure(Notification.ofError(
                CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE,
                "FCB Kafka gate CLOSED (health unknown, waiting for first probe): operation=" + operationName));
    }
}
