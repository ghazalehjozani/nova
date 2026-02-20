package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import jakarta.jms.DeliveryMode;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@Profile("activemq")
@EnableConfigurationProperties(FcbMessagingProperties.class)
public class FcbMessagingJmsConfig {

    @Bean
    public ActiveMQConnectionFactory fcbActiveMQConnectionFactory(FcbMessagingProperties properties) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(properties.brokerUrl());
        factory.setUserName(properties.credentials().username());
        factory.setPassword(properties.credentials().password());
        factory.setConnectResponseTimeout(5_000);
        factory.setTrustedPackages(
                java.util.List.of("ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto", "java.util", "java.lang"));
        return factory;
    }

    @Bean
    public JmsPoolConnectionFactory fcbPooledConnectionFactory(
            ActiveMQConnectionFactory fcbActiveMQConnectionFactory, FcbMessagingProperties properties) {
        JmsPoolConnectionFactory pooled = new JmsPoolConnectionFactory();
        pooled.setConnectionFactory(fcbActiveMQConnectionFactory);
        pooled.setMaxConnections(properties.pool().maxConnections());
        pooled.setConnectionIdleTimeout(properties.pool().idleTimeout());
        return pooled;
    }

    @Bean
    public JmsTemplate fcbValidationJmsTemplate(
            JmsPoolConnectionFactory fcbPooledConnectionFactory, FcbMessagingProperties properties) {
        return createJmsTemplate(fcbPooledConnectionFactory, properties.validation());
    }

    @Bean
    public JmsTemplate fcbAccountJmsTemplate(
            JmsPoolConnectionFactory fcbPooledConnectionFactory, FcbMessagingProperties properties) {
        return createJmsTemplate(fcbPooledConnectionFactory, properties.account());
    }

    @Bean
    public JmsTemplate fcbTransactionJmsTemplate(
            JmsPoolConnectionFactory fcbPooledConnectionFactory, FcbMessagingProperties properties) {
        return createJmsTemplate(fcbPooledConnectionFactory, properties.transaction());
    }

    private JmsTemplate createJmsTemplate(
            JmsPoolConnectionFactory connectionFactory, FcbMessagingProperties.QueueConfig queueConfig) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setDefaultDestinationName(queueConfig.requestQueue());
        template.setReceiveTimeout(queueConfig.receiveTimeoutMs());
        template.setExplicitQosEnabled(true);
        template.setDeliveryMode(DeliveryMode.PERSISTENT);
        return template;
    }
}
