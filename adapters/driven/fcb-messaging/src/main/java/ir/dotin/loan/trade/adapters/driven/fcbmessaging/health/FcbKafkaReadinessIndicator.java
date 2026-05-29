package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("fcbKafkaReadiness")
public class FcbKafkaReadinessIndicator implements HealthIndicator {

    private final ReplyingKafkaTemplate<String, byte[], byte[]> template;
    private final FcbPartitionHealthRegistry partitionRegistry;
    private final FcbReplyLeaseState leaseState;

    public FcbKafkaReadinessIndicator(
            @Qualifier("fcbHealthReplyingKafkaTemplate") ReplyingKafkaTemplate<String, byte[], byte[]> template,
            FcbPartitionHealthRegistry partitionRegistry,
            FcbReplyLeaseState leaseState) {
        this.template = template;
        this.partitionRegistry = partitionRegistry;
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
        try {
            var assignedPartitions = template.getAssignedReplyTopicPartitions();
            int assignedCount = assignedPartitions != null ? assignedPartitions.size() : 0;
            boolean hasAssignment = assignedCount > 0;

            var healthyPartitions = partitionRegistry.getHealthyPartitions();
            int healthyCount = healthyPartitions.size();

            if (hasAssignment && healthyCount > 0) {
                return Health.up()
                        .withDetail("kafkaConnection", "CONNECTED")
                        .withDetail("assignedPartitionsCount", assignedCount)
                        .withDetail("healthyPartitionsCount", healthyCount)
                        .withDetail("activePartitions", healthyPartitions)
                        .build();
            }

            // During warm-up the indicator returns UNKNOWN (Spring maps to HTTP 200) so that
            // external HTTP probes hitting the default /actuator/health do not flip to 503
            // while FCB Kafka partitions are still settling. The readiness group still
            // reflects the not-yet-UP state via the same indicator.
            Health.Builder builder = Health.unknown();
            if (!hasAssignment) {
                builder.withDetail("reason", "Awaiting Kafka metadata/assignment");
            } else {
                builder.withDetail("reason", "Metadata received but no healthy probe responses yet");
            }

            return builder.withDetail("assignedCount", assignedCount)
                    .withDetail("healthyCount", healthyCount)
                    .build();

        } catch (Exception e) {
            log.error("Readiness check failed due to unexpected error", e);
            return Health.unknown().withDetail("status", "FATAL_ERROR").build();
        }
    }
}
