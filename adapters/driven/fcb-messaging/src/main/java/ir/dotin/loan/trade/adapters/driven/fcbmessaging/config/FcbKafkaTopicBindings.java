package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import ir.dotin.platform.messaging.kafka.spi.TopicBinding;

/**
 * Single source of truth for FCB Kafka topic → groupSuffix → concurrency wiring. Each binding produces an isolated
 * consumer group; no topic appears in more than one binding.
 */
public final class FcbKafkaTopicBindings {

    private FcbKafkaTopicBindings() {}

    public static TopicBinding integrationReply(FcbKafkaProperties props, int concurrency) {
        return TopicBinding.single(props.getReplyTopic(), "fcb-integration-reply", concurrency);
    }

    public static TopicBinding healthReply(FcbKafkaProperties props) {
        // Health probe MUST be isolated — concurrency=1, dedicated group, never blocked by integration rebalance.
        return TopicBinding.single(props.getHealthReplyTopic(), "fcb-health-reply", 1);
    }
}
