package ir.dotin.loan.trade.adapters.driving.messaging.activemq.config;

import java.time.Duration;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class ActiveMqJmsConfig {

    @Value("${trade-loan.activemq.reply.timeout-ms:30000}")
    private long replyTimeoutMs;

    @Value("${trade-loan.activemq.listener.concurrency:3-10}")
    private String listenerConcurrency;

    @Bean(name = "jmsConnectionFactory")
    public ConnectionFactory jmsConnectionFactory(
            @Value("${spring.activemq.user}") String user,
            @Value("${spring.activemq.password}") String password,
            @Value("${spring.activemq.broker-url}") String brokerUrl) {
        return new ActiveMQConnectionFactory(user, password, brokerUrl);
    }

    /**
     * Listener container factory for byte-payload consumers.
     *
     * <p>Uses {@link Session#CLIENT_ACKNOWLEDGE} so the platform {@code CommandProcessor} controls acknowledgement
     * after idempotency checks and successful processing.
     */
    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            @Qualifier("jmsConnectionFactory") ConnectionFactory jmsConnectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {

        var factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, jmsConnectionFactory);
        factory.setConcurrency(listenerConcurrency);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setSessionTransacted(false);
        factory.setErrorHandler(t -> LoggerFactory.getLogger("JmsErrorHandler").error("JMS listener error", t));
        return factory;
    }

    /**
     * JmsTemplate pre-configured for request–reply.
     *
     * <p>Receive timeout governs how long {@code sendAndReceive} blocks waiting for a reply on the temporary queue.
     */
    @Bean
    public JmsTemplate jmsTemplate(@Qualifier("jmsConnectionFactory") ConnectionFactory jmsConnectionFactory) {
        var template = new JmsTemplate(jmsConnectionFactory);
        template.setReceiveTimeout(replyTimeoutMs);
        template.setDeliveryPersistent(true);
        template.setExplicitQosEnabled(true);
        template.setPubSubDomain(false);
        template.setTimeToLive(Duration.ofMinutes(5).toMillis());
        return template;
    }
    //
    //    /** Jackson-based message converter for structured JSON payloads. */
    //    @Bean
    //    public JacksonJsonMessageConverter jmsMessageConverter() {
    //        var converter = new JacksonJsonMessageConverter();
    //        converter.setTargetType(MessageType.TEXT);
    //        converter.setTypeIdPropertyName("_typeId");
    //        return converter;
    //    }
}
