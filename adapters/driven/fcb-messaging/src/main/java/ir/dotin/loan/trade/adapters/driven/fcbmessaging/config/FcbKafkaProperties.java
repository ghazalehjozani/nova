package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
     * Hard upper bound on the TOTAL wall-clock time a single {@code sendAndReceive} call may spend across the initial
     * attempt and all retries (token fetch + sign + send + reply wait + backoff). Without this bound, retrying a
     * {@code TimeoutException} re-pays the full per-call reply timeout on each of the (up to 3) attempts — worst case
     * ~3x the per-call timeout (≈180s for the 60s transaction timeout). The client shortens each retry's reply timeout
     * to fit the remaining budget so retries cannot exceed roughly one extra attempt.
     *
     * <p>When left at {@code 0} (the default), the client derives the budget per call as {@code perCallTimeout x }
     * {@link #retryBudgetMultiplier} — so existing behaviour stays bounded automatically without operators having to
     * set anything. When set to a positive value it acts as an absolute ceiling applied on top of (whichever is smaller
     * than) the derived budget.
     */
    private Duration retryMaxElapsed = Duration.ZERO;

    /**
     * Multiplier applied to the per-call reply timeout to derive the default total wall-time budget when
     * {@link #retryMaxElapsed} is unset. {@code 2.0} means "the first attempt's full timeout plus at most one extra
     * attempt's worth", which caps the previous ~3-attempt worst case at roughly 2x the per-call timeout. Must be &gt;=
     * 1.0 (a value of 1.0 disables retries' extra wall-time entirely).
     */
    @DecimalMin("1.0")
    private double retryBudgetMultiplier = 2.0;

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

    /**
     * Optional deploy-time hint for the maximum number of Nova pods expected to run concurrently. Used only to emit a
     * startup WARN when {@link #replyTopicPartitions} is too small to give every pod a private reply partition: with
     * partition-per-instance routing, once pod count exceeds {@code replyTopicPartitions} two pods collide on one
     * partition (correctness is preserved by the correlation header, but reply throughput on the shared partition
     * degrades). Default {@code 0} disables the check — existing behaviour is unchanged when this is left unset.
     */
    @Min(0)
    private int expectedMaxInstances = 0;
}
