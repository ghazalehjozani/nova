package ir.dotin.loan.trade.config.fcb;

import com.ecwid.consul.v1.ConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbReplyPartitionAssigner;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.ReplyPartitionLeaseLostListener;

import io.opentelemetry.api.OpenTelemetry;

/**
 * Wires the Consul-backed reply-partition assigner.
 *
 * <p>Spring Cloud Consul 5.x no longer auto-configures the ecwid {@code com.ecwid.consul.v1.ConsulClient} bean (its
 * native {@code org.springframework.cloud.consul.ConsulClient} has no session / KV-lock API). We therefore build a
 * dedicated ecwid client for leasing, reusing the SAME connection the app already uses for discovery + config
 * ({@link ConsulProperties} host/port/scheme — single source of truth, no duplicated host config) plus the existing ACL
 * token. The ecwid {@code consul-api} jar is on the classpath as a transitive runtime dependency.
 *
 * <p>Active when {@code nova.fcb.kafka.lease.enabled} is true (default) — which also implies the FCB Kafka transport is
 * enabled ({@code nova.fcb.kafka.enabled}). When disabled, the adapter's static fallback assigner is used. Declared
 * {@link Primary} so it wins over the fallback.
 */
@Configuration
@ConditionalOnProperty(name = "nova.fcb.kafka.lease.enabled", havingValue = "true", matchIfMissing = true)
public class FcbReplyPartitionLeaseConfig {

    private static final Logger LOG = LoggerFactory.getLogger(FcbReplyPartitionLeaseConfig.class);

    @Bean
    @Primary
    public FcbReplyPartitionAssigner consulLeaseReplyPartitionAssigner(
            ConsulProperties consulProperties,
            @Value("${consul.token:${CONSUL_TOKEN:${spring.cloud.consul.token:${SPRING_CLOUD_CONSUL_TOKEN:}}}}")
                    String aclToken,
            @Value("${platform.messaging.kafka.instance-id:${HOSTNAME:nova-service}}") String instanceId,
            FcbKafkaProperties properties,
            ObjectProvider<ReplyPartitionLeaseLostListener> leaseLostListeners,
            ObjectProvider<OpenTelemetry> openTelemetry) {
        ConsulClient consulClient = buildLeaseConsulClient(consulProperties);
        return new ConsulLeaseReplyPartitionAssigner(
                consulClient,
                aclToken,
                properties,
                instanceId,
                leaseLostListeners,
                openTelemetry.getIfAvailable(OpenTelemetry::noop).getTracer("ir.dotin.loan.fcb.reply-lease"));
    }

    /**
     * Builds an ecwid {@link ConsulClient} pointed at the same agent the rest of the app uses (reusing
     * {@link ConsulProperties} host/port). Uses the plain host:port constructor — current deployments talk to Consul
     * over HTTP (TLS is disabled in bootstrap.yml). If the scheme is https a warning is logged: Consul mTLS for the
     * lease client is out of scope and would need a {@code TLSConfig}.
     */
    private static ConsulClient buildLeaseConsulClient(ConsulProperties consulProperties) {
        String host = consulProperties.getHost().replaceFirst("^https?://", "");
        int port = consulProperties.getPort();
        String scheme = consulProperties.getScheme() == null ? "http" : consulProperties.getScheme();
        if ("https".equalsIgnoreCase(scheme)) {
            LOG.warn(
                    "FCB-REPLY-LEASE: Consul scheme=https but the lease client uses plain HTTP (mTLS not wired); "
                            + "ensure the agent at {}:{} is reachable over HTTP or extend the client with a TLSConfig.",
                    host,
                    port);
        }
        LOG.info("FCB-REPLY-LEASE: building lease Consul client for agent {}:{}", host, port);
        return new ConsulClient(host, port);
    }
}
