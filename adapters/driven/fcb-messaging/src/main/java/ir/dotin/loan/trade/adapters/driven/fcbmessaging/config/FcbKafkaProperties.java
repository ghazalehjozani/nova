package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = FcbKafkaProperties.PREFIX)
public class FcbKafkaProperties {

    public static final String PREFIX = "nova.fcb.kafka";

    @NotBlank
    private String requestTopic;

    @NotBlank
    private String replyTopic;

    @NotBlank
    private String healthRequestTopic;

    @NotBlank
    private String healthReplyTopic;

    private Duration defaultTimeout = Duration.ofSeconds(10);

    private Duration transactionTimeout = Duration.ofSeconds(60);

    /**
     * Number of partitions provisioned on {@link #replyTopic}. Each Nova pod claims exactly one partition derived from
     * its instance-id; broker provisioning must match.
     */
    @Positive
    private int replyTopicPartitions = 12;

    /**
     * Number of partitions provisioned on {@link #healthReplyTopic}. Defaults to 1 (probe traffic is low; concurrency
     * is already pinned to 1 per pod).
     */
    @Positive
    private int healthReplyTopicPartitions = 1;
}
