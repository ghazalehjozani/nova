package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = FcbKafkaProperties.PREFIX)
public record FcbKafkaProperties(
        @NotBlank String requestTopic,
        @NotBlank String replyTopic,
        @DefaultValue("10s") Duration defaultTimeout,
        @DefaultValue("60s") Duration transactionTimeout) {

    public static final String PREFIX = "nova.fcb.kafka";
}
