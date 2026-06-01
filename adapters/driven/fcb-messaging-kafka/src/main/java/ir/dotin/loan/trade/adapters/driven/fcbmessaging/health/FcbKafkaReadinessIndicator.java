package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Readiness indicator for the FCB Kafka reply path. Reports DOWN only while this instance is shedding its reply
 * partition (graceful shutdown or a lost lease) so the orchestrator drains traffic away before the reply consumer
 * stops; otherwise UP. The former active-probe partition-health signal was removed with the health-check stack.
 */
@Slf4j
@Component("fcbKafkaReadiness")
@ConditionalOnProperty(prefix = "nova.fcb.kafka", name = "enabled", matchIfMissing = true)
public class FcbKafkaReadinessIndicator implements HealthIndicator {

    private final FcbReplyLeaseState leaseState;

    public FcbKafkaReadinessIndicator(FcbReplyLeaseState leaseState) {
        this.leaseState = leaseState;
    }

    @Override
    public Health health() {
        if (leaseState.isDegraded()) {
            // Shedding the reply partition (graceful shutdown or lost lease) — report DOWN so the orchestrator drains
            // traffic away before the reply consumer stops.
            return Health.down()
                    .withDetail("reason", "FCB reply partition lease shedding: " + leaseState.reason())
                    .build();
        }
        return Health.up().build();
    }
}
