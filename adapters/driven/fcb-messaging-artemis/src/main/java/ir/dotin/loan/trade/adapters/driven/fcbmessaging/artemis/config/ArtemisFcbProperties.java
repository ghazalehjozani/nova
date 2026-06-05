package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = ArtemisFcbProperties.PREFIX)
public class ArtemisFcbProperties {

    public static final String PREFIX = "nova.fcb.artemis";

    private boolean enabled = false;

    private String brokerUrl = "tcp://localhost:61616";

    private String user = "";

    private String password = "";

    private String requestAddress = "nova.fcb.integration.request.v1";

    private String jwksRequestAddress = "nova.fcb.jwks.request.v1";

    private String replyQueuePrefix = "nova.fcb.integration.reply.";

    private String jwksReplyQueuePrefix = "nova.fcb.jwks.reply.";

    private String instanceId = "";

    private Duration replyTimeout = Duration.ofSeconds(10);

    private Duration transactionTimeout = Duration.ofSeconds(60);
}
