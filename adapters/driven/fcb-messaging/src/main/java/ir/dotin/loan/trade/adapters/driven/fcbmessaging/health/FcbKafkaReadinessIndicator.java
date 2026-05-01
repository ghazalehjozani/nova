package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("fcbKafkaReadiness")
@RequiredArgsConstructor
public class FcbKafkaReadinessIndicator extends AbstractHealthIndicator {

    private final FcbPartitionHealthRegistry partitionRegistry;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        int healthyPartitionsCount = partitionRegistry.getHealthyPartitions().size();
        if (healthyPartitionsCount > 0) {
            builder.up().withDetail("healthyPartitionsCount", healthyPartitionsCount);
        } else {
            builder.down()
                    .withDetail("reason", "No healthy FCB partitions available")
                    .withDetail("healthyPartitionsCount", 0);
        }
    }
}
