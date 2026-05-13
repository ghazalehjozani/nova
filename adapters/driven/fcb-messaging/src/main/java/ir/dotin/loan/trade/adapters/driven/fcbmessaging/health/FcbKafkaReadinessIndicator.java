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

    public FcbKafkaReadinessIndicator(
            @Qualifier("fcbHealthReplyingKafkaTemplate") ReplyingKafkaTemplate<String, byte[], byte[]> template,
            FcbPartitionHealthRegistry partitionRegistry) {
        this.template = template;
        this.partitionRegistry = partitionRegistry;
    }

    @Override
    public Health health() {
        try {
            var assignedPartitions = template.getAssignedReplyTopicPartitions();
            boolean hasAssignment = assignedPartitions != null && !assignedPartitions.isEmpty();

            var healthyPartitions = partitionRegistry.getHealthyPartitions();
            int healthyCount = healthyPartitions.size();

            if (hasAssignment && healthyCount > 0) {
                return Health.up()
                        .withDetail("kafkaConnection", "CONNECTED")
                        .withDetail("assignedPartitionsCount", assignedPartitions.size())
                        .withDetail("healthyPartitionsCount", healthyCount)
                        .withDetail("activePartitions", healthyPartitions)
                        .build();
            }

            Health.Builder builder = Health.down();
            if (!hasAssignment) {
                builder.withDetail("reason", "Awaiting Kafka metadata/assignment");
            } else {
                builder.withDetail("reason", "Metadata received but no healthy probe responses yet");
            }

            return builder.withDetail("assignedCount", hasAssignment ? assignedPartitions.size() : 0)
                    .withDetail("healthyCount", healthyCount)
                    .build();

        } catch (Exception e) {
            log.error("Readiness check failed due to unexpected error", e);
            return Health.unknown().withDetail("status", "FATAL_ERROR").build();
        }
    }
}
