package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
import org.springframework.kafka.support.TopicPartitionOffset;

import ir.dotin.platform.pangaea.messaging.autoconfigure.MessagingProperties;
import ir.dotin.platform.pangaea.messaging.kafka.spi.KafkaListenerContainerFactoryProvider;

@Configuration
@Profile("kafka-fcb")
@EnableConfigurationProperties(FcbKafkaProperties.class)
public class FcbKafkaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(FcbKafkaConfig.class);

    public static final String FCB_PRODUCER_FACTORY = "fcbProducerFactory";
    public static final String FCB_REPLY_CONSUMER_FACTORY = "fcbReplyConsumerFactory";
    public static final String FCB_INTEGRATION_REPLIES_CONTAINER = "fcbIntegrationRepliesContainer";
    public static final String FCB_HEALTH_REPLIES_CONTAINER = "fcbHealthRepliesContainer";
    public static final String FCB_INTEGRATION_REPLYING_TEMPLATE = "fcbIntegrationReplyingKafkaTemplate";
    public static final String FCB_HEALTH_REPLYING_TEMPLATE = "fcbHealthReplyingKafkaTemplate";
    public static final String FCB_INTEGRATION_REPLY_PARTITION = "fcbIntegrationReplyPartition";
    public static final String FCB_HEALTH_REPLY_PARTITION = "fcbHealthReplyPartition";
    public static final String GENERAL_REPLIES_CONTAINER = "generalRepliesContainer";

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
                messagingProperties.getKafka().getBootstrapServers());
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
                messagingProperties.getKafka().getBootstrapServers());
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
        configs.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, (int) messagingProperties
                .getKafka()
                .getConsumer()
                .getDefaultApiTimeout()
                .toMillis());
        configs.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int)
                messagingProperties.getKafka().getConsumer().getRequestTimeout().toMillis());
        configs.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, (int)
                messagingProperties.getKafka().getConsumer().getMetadataMaxAge().toMillis());
        applySecurity(configs, messagingProperties);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    /**
     * Fallback assigner for test slices / Consul-less contexts. In real deployments
     * {@code ConsulLeaseReplyPartitionAssigner} (container module, {@code @Primary}) wins, so this conditional bean is
     * absent.
     */
    @Bean
    @ConditionalOnMissingBean(FcbReplyPartitionAssigner.class)
    public FcbReplyPartitionAssigner staticFcbReplyPartitionAssigner(MessagingProperties messagingProperties) {
        return new StaticFcbReplyPartitionAssigner(
                messagingProperties.getKafka().getInstanceId());
    }

    /**
     * Claims this instance's unique reply partition for its lifetime. The lease range is the broker's actual partition
     * count for the reply topic (authoritative), falling back to {@code reply-topic-partitions} only if metadata is
     * unavailable. The {@link ReplyPartitionLease} is also consumed by {@code FcbReplyDrainCoordinator} to release on
     * shutdown / lost lease.
     */
    @Bean
    public ReplyPartitionLease fcbIntegrationReplyPartitionLease(
            FcbReplyPartitionAssigner assigner,
            FcbKafkaProperties properties,
            @Qualifier(FCB_REPLY_CONSUMER_FACTORY) ConsumerFactory<String, byte[]> fcbReplyConsumerFactory) {
        int partitionCount = resolvePartitionCount(
                fcbReplyConsumerFactory, properties.getReplyTopic(), properties.getReplyTopicPartitions());
        warnIfPartitionsTooFewForExpectedInstances(partitionCount, properties.getExpectedMaxInstances());
        return assigner.acquire(properties.getReplyTopic(), partitionCount);
    }

    @Bean(FCB_INTEGRATION_REPLY_PARTITION)
    public int fcbIntegrationReplyPartition(ReplyPartitionLease fcbIntegrationReplyPartitionLease) {
        return fcbIntegrationReplyPartitionLease.partition();
    }

    /**
     * Authoritative partition count from broker metadata, so the lease scan range always matches the provisioned reply
     * topic (avoiding drift between the {@code reply-topic-partitions} config default and the actual broker topic).
     */
    private static int resolvePartitionCount(
            ConsumerFactory<String, byte[]> consumerFactory, String replyTopic, int configuredCount) {
        try (Consumer<String, byte[]> consumer = consumerFactory.createConsumer()) {
            List<PartitionInfo> infos = consumer.partitionsFor(replyTopic);
            if (infos == null || infos.isEmpty()) {
                LOG.warn(
                        "FCB-REPLY-PARTITION: broker returned no partition metadata for reply topic '{}'; "
                                + "falling back to configured reply-topic-partitions={}",
                        replyTopic,
                        configuredCount);
                return configuredCount;
            }
            int brokerCount = infos.size();
            if (brokerCount != configuredCount) {
                LOG.warn(
                        "FCB-REPLY-PARTITION: broker reply topic '{}' has {} partitions but configured "
                                + "reply-topic-partitions={}; using broker value {} as the lease range",
                        replyTopic,
                        brokerCount,
                        configuredCount,
                        brokerCount);
            }
            return brokerCount;
        }
    }

    /**
     * Emits a startup WARN when the reply topic has fewer partitions than the expected pod count, since
     * partition-per-instance routing then forces two pods onto one partition (correctness preserved by the correlation
     * header; throughput on the shared partition degrades). No-op unless {@code expectedMaxInstances > 0}, so the check
     * is disabled by default and existing behaviour is unchanged when the hint is unset.
     */
    private static void warnIfPartitionsTooFewForExpectedInstances(int partitionCount, int expectedMaxInstances) {
        if (expectedMaxInstances > 0 && partitionCount < expectedMaxInstances) {
            LOG.warn(
                    "FCB-REPLY-PARTITION: reply-topic-partitions={} is smaller than expected-max-instances={}; "
                            + "with partition-per-instance routing, pods beyond partition {} will collide on a shared "
                            + "reply partition. Provision reply-topic-partitions >= expected-max-instances on the broker "
                            + "(and match FCB's OperationCategory.NOVA_* config).",
                    partitionCount,
                    expectedMaxInstances,
                    partitionCount - 1);
        }
    }

    @Bean(FCB_HEALTH_REPLY_PARTITION)
    public int fcbHealthReplyPartition(FcbKafkaProperties properties, MessagingProperties messagingProperties) {
        return FcbReplyPartitionResolver.resolve(
                messagingProperties.getKafka().getInstanceId(), properties.getHealthReplyTopicPartitions());
    }

    @Bean(FCB_INTEGRATION_REPLIES_CONTAINER)
    public ConcurrentMessageListenerContainer<String, byte[]> fcbIntegrationRepliesContainer(
            @Qualifier(FCB_REPLY_CONSUMER_FACTORY) ConsumerFactory<String, byte[]> fcbReplyConsumerFactory,
            FcbKafkaProperties properties,
            @Qualifier(FCB_INTEGRATION_REPLY_PARTITION) int partition,
            KafkaListenerContainerFactoryProvider provider) {
        return manualAssignContainer(
                fcbReplyConsumerFactory,
                provider.groupIdFor("core.loan.nova.fcb-integration-reply"),
                properties.getReplyTopic(),
                partition);
    }

    @Bean(FCB_HEALTH_REPLIES_CONTAINER)
    public ConcurrentMessageListenerContainer<String, byte[]> fcbHealthRepliesContainer(
            @Qualifier(FCB_REPLY_CONSUMER_FACTORY) ConsumerFactory<String, byte[]> fcbReplyConsumerFactory,
            FcbKafkaProperties properties,
            @Qualifier(FCB_HEALTH_REPLY_PARTITION) int partition,
            KafkaListenerContainerFactoryProvider provider) {
        return manualAssignContainer(
                fcbReplyConsumerFactory,
                provider.groupIdFor("core.loan.nova.fcb-health-reply"),
                properties.getHealthReplyTopic(),
                partition);
    }

    private static ConcurrentMessageListenerContainer<String, byte[]> manualAssignContainer(
            ConsumerFactory<String, byte[]> consumerFactory, String groupId, String topic, int partition) {
        ContainerProperties props = new ContainerProperties(new TopicPartitionOffset(topic, partition));
        props.setGroupId(groupId);
        props.setAckMode(ContainerProperties.AckMode.BATCH);
        ConcurrentMessageListenerContainer<String, byte[]> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, props);
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
        template.setDefaultReplyTimeout(properties.getDefaultTimeout());
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

    /**
     * General whole-reply-topic container backing {@link #replyingKafkaTemplate}. Subscribes the entire integration
     * reply topic (no partition pin), in a per-instance consumer group, so the actor-envelope JWKS-over-Kafka
     * request/reply can receive its reply regardless of partition. Replies are correlation-id matched, so multiple
     * instances each receiving a copy is correct (just mildly redundant) — JWKS fetches are infrequent.
     */
    @Bean(GENERAL_REPLIES_CONTAINER)
    public ConcurrentMessageListenerContainer<String, byte[]> generalRepliesContainer(
            @Qualifier(FCB_REPLY_CONSUMER_FACTORY) ConsumerFactory<String, byte[]> fcbReplyConsumerFactory,
            FcbKafkaProperties properties,
            KafkaListenerContainerFactoryProvider provider) {

        ContainerProperties props = new ContainerProperties(properties.getReplyTopic());
        props.setGroupId(provider.groupIdFor("core.loan.nova.general-reply"));
        props.setAckMode(ContainerProperties.AckMode.BATCH);

        ConcurrentMessageListenerContainer<String, byte[]> container =
                new ConcurrentMessageListenerContainer<>(fcbReplyConsumerFactory, props);
        container.setConcurrency(1);
        container.setAutoStartup(true);
        return container;
    }

    /**
     * DO NOT REMOVE / DO NOT RENAME. Although the FCB request/reply client uses
     * {@link #FCB_INTEGRATION_REPLYING_TEMPLATE} and the probe uses {@link #FCB_HEALTH_REPLYING_TEMPLATE}, this third
     * {@code ReplyingKafkaTemplate} is consumed cross-repo by pangaea's
     * {@code ActorEnvelopeAutoConfiguration.envelopeKafkaJwksFetcher}, which injects it BY PARAMETER NAME
     * ({@code replyingKafkaTemplate}) for the actor-envelope JWKS-over-Kafka fetch. Removing it leaves two
     * {@code ReplyingKafkaTemplate} candidates and the by-name injection can no longer disambiguate → context fails to
     * start ("required a single bean, but 2 were found"). A repo-local grep does not reveal this consumer.
     */
    @Bean("replyingKafkaTemplate")
    public ReplyingKafkaTemplate<String, byte[], byte[]> replyingKafkaTemplate(
            @Qualifier(FCB_PRODUCER_FACTORY) ProducerFactory<String, byte[]> fcbProducerFactory,
            @Qualifier(GENERAL_REPLIES_CONTAINER)
                    ConcurrentMessageListenerContainer<String, byte[]> generalRepliesContainer,
            FcbKafkaProperties properties) {

        ReplyingKafkaTemplate<String, byte[], byte[]> template =
                new ReplyingKafkaTemplate<>(fcbProducerFactory, generalRepliesContainer);
        template.setSharedReplyTopic(true);
        template.setBinaryCorrelation(false);
        template.setDefaultReplyTimeout(properties.getDefaultTimeout());
        return template;
    }

    private static void applySecurity(Map<String, Object> configs, MessagingProperties messagingProperties) {
        var security = messagingProperties.getKafka().getSecurity();
        if (security == null || security.getProtocol() == null) {
            return;
        }
        configs.put("security.protocol", security.getProtocol());
        configs.put("sasl.mechanism", security.getSaslMechanism());
        configs.put("sasl.jaas.config", security.getSaslJaasConfig());
    }
}
