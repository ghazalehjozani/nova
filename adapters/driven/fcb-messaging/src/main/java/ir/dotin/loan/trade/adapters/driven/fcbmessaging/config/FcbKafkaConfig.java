package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import ir.dotin.platform.messaging.autoconfigure.MessagingProperties;
import ir.dotin.platform.messaging.kafka.autoconfigure.KafkaMessagingAutoConfiguration;

@Configuration
@Profile("kafka-fcb")
@EnableConfigurationProperties(FcbKafkaProperties.class)
public class FcbKafkaConfig {

    public static final String FCB_PRODUCER_FACTORY = "fcbProducerFactory";
    public static final String FCB_REPLY_CONSUMER_FACTORY = "fcbReplyConsumerFactory";
    public static final String FCB_INTEGRATION_REPLIES_CONTAINER = "fcbIntegrationRepliesContainer";
    public static final String FCB_HEALTH_REPLIES_CONTAINER = "fcbHealthRepliesContainer";
    public static final String FCB_INTEGRATION_REPLYING_TEMPLATE = "fcbIntegrationReplyingKafkaTemplate";
    public static final String FCB_HEALTH_REPLYING_TEMPLATE = "fcbHealthReplyingKafkaTemplate";

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final int PRODUCER_BATCH_SIZE_BYTES = 131_072;
    private static final long PRODUCER_LINGER_MS = 100L;
    private static final int REPLY_MAX_POLL_RECORDS = 500;
    private static final int REPLY_FETCH_MIN_BYTES = 1;
    private static final int REPLY_FETCH_MAX_WAIT_MS = 50;
    private static final int REPLY_AUTO_COMMIT_INTERVAL_MS = 1_000;
    private static final int PRODUCER_DELIVERY_TIMEOUT_MS = 120_000;
    private static final int PRODUCER_REQUEST_TIMEOUT_MS = 30_000;

    @Bean(FCB_PRODUCER_FACTORY)
    public ProducerFactory<String, byte[]> fcbProducerFactory(MessagingProperties messagingProperties) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                messagingProperties.kafka().bootstrapServers());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configs.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, PRODUCER_BATCH_SIZE_BYTES);
        configs.put(ProducerConfig.LINGER_MS_CONFIG, PRODUCER_LINGER_MS);
        configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configs.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, PRODUCER_DELIVERY_TIMEOUT_MS);
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, PRODUCER_REQUEST_TIMEOUT_MS);
        applySecurity(configs, messagingProperties);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean(FCB_REPLY_CONSUMER_FACTORY)
    public ConsumerFactory<String, byte[]> fcbReplyConsumerFactory(MessagingProperties messagingProperties) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                messagingProperties.kafka().bootstrapServers());
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        configs.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, REPLY_AUTO_COMMIT_INTERVAL_MS);
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, REPLY_MAX_POLL_RECORDS);
        configs.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, REPLY_FETCH_MIN_BYTES);
        configs.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, REPLY_FETCH_MAX_WAIT_MS);
        configs.put(ConsumerConfig.GROUP_PROTOCOL_CONFIG, "classic");
        configs.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());
        applySecurity(configs, messagingProperties);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean(FCB_INTEGRATION_REPLIES_CONTAINER)
    public ConcurrentMessageListenerContainer<String, byte[]> fcbIntegrationRepliesContainer(
            @Qualifier(FCB_REPLY_CONSUMER_FACTORY) ConsumerFactory<String, byte[]> fcbReplyConsumerFactory,
            FcbKafkaProperties properties,
            @Value("${platform.messaging.kafka.consumer-group-id}") String baseGroupId) {

        String uniqueReplyGroupId =
                baseGroupId + ".fcb-integration-reply." + KafkaMessagingAutoConfiguration.INSTANCE_ID;

        ContainerProperties containerProps = new ContainerProperties(properties.replyTopic());
        containerProps.setGroupId(uniqueReplyGroupId);
        containerProps.setAckMode(ContainerProperties.AckMode.BATCH);

        ConcurrentMessageListenerContainer<String, byte[]> container =
                new ConcurrentMessageListenerContainer<>(fcbReplyConsumerFactory, containerProps);
        container.setConcurrency(Math.max(1, CORES));
        container.setAutoStartup(true);
        return container;
    }

    @Bean(FCB_HEALTH_REPLIES_CONTAINER)
    public ConcurrentMessageListenerContainer<String, byte[]> fcbHealthRepliesContainer(
            @Qualifier(FCB_REPLY_CONSUMER_FACTORY) ConsumerFactory<String, byte[]> fcbReplyConsumerFactory,
            FcbKafkaProperties properties,
            @Value("${platform.messaging.kafka.consumer-group-id}") String baseGroupId) {

        String uniqueReplyGroupId = baseGroupId + ".fcb-health-reply." + KafkaMessagingAutoConfiguration.INSTANCE_ID;

        ContainerProperties containerProps = new ContainerProperties(properties.healthReplyTopic());
        containerProps.setGroupId(uniqueReplyGroupId);
        containerProps.setAckMode(ContainerProperties.AckMode.BATCH);

        ConcurrentMessageListenerContainer<String, byte[]> container =
                new ConcurrentMessageListenerContainer<>(fcbReplyConsumerFactory, containerProps);
        container.setConcurrency(1);
        container.setAutoStartup(true);
        return container;
    }

    @Bean(FCB_INTEGRATION_REPLYING_TEMPLATE)
    public ReplyingKafkaTemplate<String, byte[], byte[]> fcbIntegrationReplyingKafkaTemplate(
            @Qualifier(FCB_PRODUCER_FACTORY) ProducerFactory<String, byte[]> fcbProducerFactory,
            @Qualifier(FCB_INTEGRATION_REPLIES_CONTAINER)
                    ConcurrentMessageListenerContainer<String, byte[]> fcbIntegrationRepliesContainer,
            FcbKafkaProperties properties) {
        ReplyingKafkaTemplate<String, byte[], byte[]> template =
                new ReplyingKafkaTemplate<>(fcbProducerFactory, fcbIntegrationRepliesContainer);
        template.setSharedReplyTopic(true);
        template.setBinaryCorrelation(false);
        template.setDefaultReplyTimeout(properties.defaultTimeout());
        return template;
    }

    @Bean(FCB_HEALTH_REPLYING_TEMPLATE)
    public ReplyingKafkaTemplate<String, byte[], byte[]> fcbHealthReplyingKafkaTemplate(
            @Qualifier(FCB_PRODUCER_FACTORY) ProducerFactory<String, byte[]> fcbProducerFactory,
            @Qualifier(FCB_HEALTH_REPLIES_CONTAINER)
                    ConcurrentMessageListenerContainer<String, byte[]> fcbHealthRepliesContainer) {
        ReplyingKafkaTemplate<String, byte[], byte[]> template =
                new ReplyingKafkaTemplate<>(fcbProducerFactory, fcbHealthRepliesContainer);
        template.setSharedReplyTopic(true);
        template.setBinaryCorrelation(false);
        return template;
    }

    private static void applySecurity(Map<String, Object> configs, MessagingProperties messagingProperties) {
        var security = messagingProperties.kafka().security();
        if (security == null || security.protocol() == null) {
            return;
        }
        configs.put("security.protocol", security.protocol());
        configs.put("sasl.mechanism", security.saslMechanism());
        configs.put("sasl.jaas.config", security.saslJaasConfig());
    }
}
