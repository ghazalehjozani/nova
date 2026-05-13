package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcbHealthGate {

    private final FcbPartitionHealthRegistry partitionRegistry;
    private final FcbHealthProperties properties;

    public Result<Void> checkPermitted(String operationName) {
        if (partitionRegistry.healthyCount() > 0) {
            return Result.success();
        }
        if (properties.isFailOpenOnUnknown()) {
            if (partitionRegistry.totalKnown() == 0) {
                log.debug("FCB-GATE: no probe data yet, fail-open op={}", operationName);
                return Result.success();
            }
            if (partitionRegistry.anyPartitionStillWarming()) {
                log.debug("FCB-GATE: partitions warming up, fail-open op={}", operationName);
                return Result.success();
            }
        }
        return Result.failure(Notification.ofError(
                CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE,
                "FCB Kafka gate CLOSED (no healthy partitions): operation=" + operationName));
    }
}
