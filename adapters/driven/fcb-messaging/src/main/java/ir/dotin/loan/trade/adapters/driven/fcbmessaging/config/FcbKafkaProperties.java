package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.time.Duration;
import jakarta.validation.constraints.NotBlank;

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
}
