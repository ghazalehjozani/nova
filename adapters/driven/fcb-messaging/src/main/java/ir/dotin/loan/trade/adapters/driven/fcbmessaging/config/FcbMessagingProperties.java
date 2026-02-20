package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for FCB ActiveMQ messaging integration. Bound from {@code nova.fcb.activemq.*} in
 * application.yml.
 *
 * <p>Example:
 *
 * <pre>
 * nova:
 *   fcb:
 *     activemq:
 *       broker-url: tcp://localhost:61616
 *       credentials:
 *         username: admin
 *         password: admin
 *       pool:
 *         max-connections: 10
 *       queues:
 *         validation:
 *           request-queue: corridor.core.loan.fcb.validation.request.queue.v1
 *           reply-queue: corridor.core.loan.fcb.validation.response.queue.v1
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = FcbMessagingProperties.PREFIX)
public record FcbMessagingProperties(
        @NotBlank String brokerUrl,
        @Valid @NotNull Credentials credentials,
        @Valid @NotNull Pool pool,
        @Valid @NotNull Queues queues) {

    public static final String PREFIX = "nova.fcb.activemq";

    public record Credentials(
            @NotBlank String username, @NotBlank String password) {}

    public record Pool(
            @DefaultValue("10") @Positive int maxConnections,
            @DefaultValue("30000") @Positive int idleTimeout,
            @DefaultValue("60000") @Positive long expiryTimeout) {}

    public record Queues(
            @Valid @NotNull QueueConfig validation,
            @Valid @NotNull QueueConfig account,
            @Valid @NotNull QueueConfig transaction) {}

    public record QueueConfig(
            @NotBlank String requestQueue,
            @NotBlank String replyQueue,
            @DefaultValue("30000") @Positive long receiveTimeoutMs) {}

    /** Shortcut accessors for backward-compatible queue access. */
    public QueueConfig validation() {
        return queues.validation();
    }

    public QueueConfig account() {
        return queues.account();
    }

    public QueueConfig transaction() {
        return queues.transaction();
    }
}
