package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import ir.dotin.platform.messaging.kafka.autoconfigure.KafkaMessagingAutoConfiguration;

@Configuration
@Profile("kafka-fcb")
@EnableConfigurationProperties(FcbKafkaProperties.class)
public class FcbKafkaConfig {

    @Bean(name = "fcbHealthRepliesContainer")
    public ConcurrentMessageListenerContainer<String, byte[]> fcbHealthRepliesContainer(
            @Qualifier("replyConsumerFactory") ConsumerFactory<String, byte[]> replyConsumerFactory,
            FcbKafkaProperties properties,
            @Value("${platform.messaging.kafka.consumer-group-id}") String baseGroupId) {

        String uniqueReplyGroupId = baseGroupId + ".health-reply." + KafkaMessagingAutoConfiguration.INSTANCE_ID;

        ContainerProperties containerProps = new ContainerProperties(properties.healthReplyTopic());
        containerProps.setGroupId(uniqueReplyGroupId);
        containerProps.setAckMode(ContainerProperties.AckMode.BATCH);

        ConcurrentMessageListenerContainer<String, byte[]> container =
                new ConcurrentMessageListenerContainer<>(replyConsumerFactory, containerProps);
        container.setConcurrency(1);
        container.setAutoStartup(true);
        return container;
    }

    @Bean(name = "fcbHealthReplyingKafkaTemplate")
    public ReplyingKafkaTemplate<String, byte[], byte[]> fcbHealthReplyingKafkaTemplate(
            @Qualifier("byteArrayProducerFactory") ProducerFactory<String, byte[]> producerFactory,
            @Qualifier("fcbHealthRepliesContainer")
                    ConcurrentMessageListenerContainer<String, byte[]> fcbHealthRepliesContainer) {

        ReplyingKafkaTemplate<String, byte[], byte[]> template =
                new ReplyingKafkaTemplate<>(producerFactory, fcbHealthRepliesContainer);
        template.setSharedReplyTopic(true);
        template.setBinaryCorrelation(false);
        return template;
    }
}
