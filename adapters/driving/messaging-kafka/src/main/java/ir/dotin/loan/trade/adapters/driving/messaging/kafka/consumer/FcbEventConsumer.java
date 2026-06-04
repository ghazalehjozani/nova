package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer;

import java.nio.charset.StandardCharsets;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.inbox.core.InboundEventIngestor;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.pangaea.messaging.kafka.converter.KafkaInboundMessageConverter;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(prefix = "nova.driving.messaging-kafka.fcb-event", name = "enabled", matchIfMissing = false)
public class FcbEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(FcbEventConsumer.class);

    private static final String HEADER_CORRELATION_TRACEPARENT = "correlation-traceparent";
    private static final String LEGACY_PEER_SERVICE = "fcb-legacy";
    private static final String ATTR_PEER_SERVICE = "peer.service";
    private static final String ATTR_CORRELATION_TRACE_ID = "correlation.trace.id";
    private static final String ATTR_CORRELATION_SPAN_ID = "correlation.span.id";
    private static final int TRACEPARENT_LENGTH = 55;

    private final ObjectMapper objectMapper;
    private final KafkaInboundMessageConverter converter;
    private final InboundEventIngestor ingestor;

    @Nullable
    private final Tracer tracer;

    public FcbEventConsumer(
            ObjectMapper objectMapper,
            KafkaInboundMessageConverter converter,
            InboundEventIngestor ingestor,
            @Nullable Tracer tracer) {
        this.objectMapper = objectMapper;
        this.converter = converter;
        this.ingestor = ingestor;
        this.tracer = tracer;
    }

    @KafkaListener(
            topics = "corridor.core.loan.nova.installment-operation.request.queue.v1",
            groupId = "${platform.messaging.kafka.consumer-group-id}",
            containerFactory = "byteArrayKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, byte[]> consumerRecord) {
        try {
            applyFcbLegacyAttributes(consumerRecord);
            InboundMessage inboundMessage = converter.convert(consumerRecord);
            JsonNode rootNode = objectMapper.readTree(inboundMessage.payload());

            String operationType = resolveOperationType(rootNode, consumerRecord);
            String eventUid = resolveEventUid(consumerRecord);

            FcbEventOperationType opType;
            try {
                opType = FcbEventOperationType.ofCode(operationType);
            } catch (IllegalArgumentException e) {
                LOG.warn("Unknown operationType [{}], eventUid={}, skipping.", operationType, eventUid);
                return;
            }

            InboundMessage message = inboundMessage.withSource(opType.getCode());
            ingestor.ingest(message);
        } catch (Exception e) {
            LOG.error(
                    "Fatal error processing record. offset={}, partition={}, key={}. Skipping to save batch.",
                    consumerRecord.offset(),
                    consumerRecord.partition(),
                    consumerRecord.key(),
                    e);
        }
    }

    private String resolveOperationType(JsonNode rootNode, ConsumerRecord<String, byte[]> record) {
        // 1. Try header first (cheap)
        String fromHeader = extractHeader(record, "operationType");
        if (fromHeader != null && !fromHeader.isEmpty()) {
            return fromHeader;
        }
        // 2. Fallback to body
        JsonNode opNode = rootNode.get("operationType");
        if (opNode != null && !opNode.isNull()) {
            return opNode.asString();
        }
        return "UNKNOWN";
    }

    @Nullable
    private String resolveEventUid(ConsumerRecord<String, byte[]> record) {
        String fromHeader = extractHeader(record, "eventUid");
        if (fromHeader != null && !fromHeader.isEmpty()) {
            return fromHeader;
        }
        return null;
    }

    @Nullable
    private String extractHeader(ConsumerRecord<String, byte[]> record, String headerName) {
        var header = record.headers().lastHeader(headerName);
        if (header != null && header.value() != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }

    private void applyFcbLegacyAttributes(ConsumerRecord<String, byte[]> record) {
        if (tracer == null) {
            return;
        }
        Span current = tracer.currentSpan();
        if (current == null) {
            return;
        }
        current.tag(ATTR_PEER_SERVICE, LEGACY_PEER_SERVICE);
        Header header = record.headers().lastHeader(HEADER_CORRELATION_TRACEPARENT);
        if (header == null || header.value() == null) {
            return;
        }
        String corr = new String(header.value(), StandardCharsets.UTF_8).trim();
        if (corr.length() != TRACEPARENT_LENGTH) {
            return;
        }
        String[] parts = corr.split("-");
        if (parts.length != 4
                || parts[1].length() != 32
                || parts[2].length() != 16
                || !isHex(parts[1])
                || !isHex(parts[2])) {
            return;
        }
        current.tag(ATTR_CORRELATION_TRACE_ID, parts[1]);
        current.tag(ATTR_CORRELATION_SPAN_ID, parts[2]);
    }

    private static boolean isHex(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }
        return true;
    }
}
