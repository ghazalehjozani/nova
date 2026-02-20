package ir.dotin.loan.trade.adapters.driven.fcbmessaging.health;

import jakarta.jms.Connection;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbMessagingProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("activemq")
@RequiredArgsConstructor
public class FcbJmsHealthIndicator implements HealthIndicator {

    private final JmsPoolConnectionFactory fcbPooledConnectionFactory;
    private final FcbMessagingProperties properties;

    @Override
    public Health health() {
        try (Connection connection = fcbPooledConnectionFactory.createConnection()) {
            connection.start();
            return Health.up()
                    .withDetail("brokerUrl", properties.brokerUrl())
                    .withDetail("maxConnections", properties.pool().maxConnections())
                    .build();
        } catch (Exception e) {
            log.error("ActiveMQ broker health check failed", e);
            return Health.down()
                    .withDetail("brokerUrl", properties.brokerUrl())
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
