package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.micrometer.core.instrument.Counter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FcbHealthGate {

    private final FcbPartitionHealthRegistry partitionRegistry;
    private final FcbHealthProperties properties;
    private final Counter deniedCounter;

    public FcbHealthGate(
            FcbPartitionHealthRegistry partitionRegistry,
            FcbHealthProperties properties,
            @Qualifier("fcbHealthGateDeniedCounter") Counter deniedCounter) {
        this.partitionRegistry = partitionRegistry;
        this.properties = properties;
        this.deniedCounter = deniedCounter;
    }

    public Result<Void> checkPermitted(String operationName) {
        if (partitionRegistry.healthyCount() > 0) {
            return Result.success();
        }

        boolean warmingUp = !partitionRegistry.hasReachedFirstSuccessfulCycle();
        boolean failClosed = properties.getStartupMode() == FcbHealthProperties.StartupMode.FAIL_CLOSED;
        if (warmingUp && failClosed) {
            deniedCounter.increment();
            log.debug("FCB-GATE: warmup fail-closed (no successful probe yet) op={}", operationName);
            return Result.failure(Notification.ofError(
                    CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE,
                    "FCB Kafka gate CLOSED (warming up, fail-closed): operation=" + operationName));
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

        deniedCounter.increment();
        return Result.failure(Notification.ofError(
                CoreBankingErrors.KAFKA_BROKER_UNAVAILABLE,
                "FCB Kafka gate CLOSED (no healthy partitions): operation=" + operationName));
    }
}
