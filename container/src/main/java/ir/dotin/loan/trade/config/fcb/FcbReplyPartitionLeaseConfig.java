package ir.dotin.loan.trade.config.fcb;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbReplyPartitionAssigner;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.ReplyPartitionLeaseLostListener;

/**
 * Wires the Consul-backed reply-partition assigner. Lives in the container module because the ecwid
 * {@link ConsulClient} (from {@code spring-cloud-consul}) is only on this module's classpath. Active under the
 * {@code kafka-fcb} profile and when {@code nova.fcb.kafka.lease.enabled} is true (default). When disabled, the
 * adapter's static fallback assigner is used instead. Declared {@link Primary} so it wins over the fallback whenever
 * both are present.
 */
@Configuration
@Profile("kafka-fcb")
@ConditionalOnProperty(name = "nova.fcb.kafka.lease.enabled", havingValue = "true", matchIfMissing = true)
public class FcbReplyPartitionLeaseConfig {

    @Bean
    @Primary
    public FcbReplyPartitionAssigner consulLeaseReplyPartitionAssigner(
            ConsulClient consulClient,
            @Value("${CONSUL_ACL_TOKEN:}") String aclToken,
            @Value("${platform.messaging.kafka.instance-id:${HOSTNAME:nova-service}}") String instanceId,
            FcbKafkaProperties properties,
            ObjectProvider<ReplyPartitionLeaseLostListener> leaseLostListeners) {
        return new ConsulLeaseReplyPartitionAssigner(
                consulClient, aclToken, properties, instanceId, leaseLostListeners);
    }
}
