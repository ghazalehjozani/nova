package ir.dotin.loan.trade.config.fcb;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config.ArtemisFcbConfig;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.service.FcbKafkaClient;

/**
 * Composition-root router that selects the active Nova ↔ FCB request/reply transport per call.
 *
 * <p>This is the single switch point. The transport-neutral port adapters in {@code fcb-messaging-contract} inject the
 * {@link FcbRequestReplyClient} seam and — because this router is the only {@link Primary} implementation — get
 * <em>it</em> with no ambiguity. The two delegate clients ({@code kafkaFcbRequestReplyClient},
 * {@code artemisFcbRequestReplyClient}) are non-{@code @Primary} qualified beans.
 *
 * <p>Both delegates are injected via {@link ObjectProvider} so the router wires even when Artemis is disabled (the
 * default): with {@code nova.fcb.artemis.enabled=false} no Artemis bean exists, and {@code transport-mode=artemis}
 * degrades to Kafka rather than failing context startup. {@link FcbTransportProperties} is read per call so a live
 * Consul flip of {@code transport-mode} re-points the corridor with no redeploy.
 */
@Component
@Primary
public class RoutingFcbRequestReplyClient implements FcbRequestReplyClient {

    private static final Logger LOG = LoggerFactory.getLogger(RoutingFcbRequestReplyClient.class);

    private final FcbTransportProperties properties;
    private final ObjectProvider<FcbRequestReplyClient> kafka;
    private final ObjectProvider<FcbRequestReplyClient> artemis;

    public RoutingFcbRequestReplyClient(
            FcbTransportProperties properties,
            @Qualifier(FcbKafkaClient.KAFKA_FCB_CLIENT) ObjectProvider<FcbRequestReplyClient> kafka,
            @Qualifier(ArtemisFcbConfig.ARTEMIS_FCB_CLIENT) ObjectProvider<FcbRequestReplyClient> artemis) {
        this.properties = properties;
        this.kafka = kafka;
        this.artemis = artemis;
    }

    @Override
    public Result<FcbKafkaBaseResponse> sendAndReceive(FcbKafkaBaseRequest request, Duration timeout) {
        return active().sendAndReceive(request, timeout);
    }

    private FcbRequestReplyClient active() {
        boolean wantArtemis = properties.getTransportMode() == FcbTransportProperties.TransportMode.ARTEMIS;
        if (wantArtemis) {
            FcbRequestReplyClient a = artemis.getIfAvailable();
            if (a != null) {
                return a;
            }
            LOG.warn("FCB-TRANSPORT: transport-mode=artemis but the Artemis client is not wired "
                    + "(nova.fcb.artemis.enabled=false); falling back to kafka");
        }
        FcbRequestReplyClient k = kafka.getIfAvailable();
        if (k != null) {
            return k;
        }
        // Kafka is the default selection but its client is absent (nova.fcb.kafka.enabled=false). Fall to Artemis
        // if it is wired rather than fail — symmetric to the artemis-absent branch above.
        FcbRequestReplyClient a = artemis.getIfAvailable();
        if (a != null) {
            if (!wantArtemis) {
                LOG.warn("FCB-TRANSPORT: transport-mode=kafka but the Kafka client is not wired "
                        + "(nova.fcb.kafka.enabled=false); using artemis");
            }
            return a;
        }
        throw new IllegalStateException("No FCB request/reply transport is wired — enable nova.fcb.kafka.enabled "
                + "or nova.fcb.artemis.enabled");
    }
}
