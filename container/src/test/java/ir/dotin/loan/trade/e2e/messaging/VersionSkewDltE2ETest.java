package ir.dotin.loan.trade.e2e.messaging;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import ir.dotin.platform.adapter.messaging.persistence.entity.DeadLetterEntity;
import ir.dotin.platform.adapter.messaging.persistence.repository.DeadLetterRepository;
import ir.dotin.platform.messaging.api.header.MessagingHeaderNames;
import ir.dotin.platform.messaging.api.version.VersionSkewDecision;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.LoanFacilityRestructuringMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler.LoanFacilityRestructuringHandler;
import ir.dotin.loan.trade.e2e.AbstractMessagingE2E;
import ir.dotin.loan.trade.e2e.fixture.KafkaTestHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Verifies that a producer publishing on a version below the consumer's {@code minProducerVersion} is rejected by the
 * pangaea contract-version filter, lands in the dead-letter store, and never reaches the handler.
 *
 * <p>The Nova consumers are pinned at {@code @ContractVersion(1)} (defaults {@code minProducerVersion=1}). A record
 * stamped with {@code Contract-Version: 0} therefore resolves to {@link VersionSkewDecision#STALE_PRODUCER} and the
 * inbox processor routes it to the dead-letter store after the retry budget is exhausted.
 *
 * <p>{@code platform.messaging.inbox.max-retries=1} is set so the DLT lands on the first failure — keeps the test fast
 * and deterministic without changing production semantics.
 */
@TestPropertySource(properties = {"platform.messaging.inbox.max-retries=1"})
class VersionSkewDltE2ETest extends AbstractMessagingE2E {

    private static final String FCB_EVENT_TOPIC = "corridor.core.loan.nova.installment-operation.request.queue.v1";

    @Autowired
    private DeadLetterRepository deadLetterRepository;

    @MockitoSpyBean
    private LoanFacilityRestructuringHandler restructuringHandler;

    @Test
    void shouldRouteToDltWhenProducerVersionIsStale() throws Exception {
        String eventUid = UUID.randomUUID().toString();

        LoanFacilityRestructuringMessage staleMessage = new LoanFacilityRestructuringMessage(
                "E2E_PRODUCER",
                eventUid,
                new Date(),
                0,
                null,
                null,
                FcbEventOperationType.LOAN_FACILITY_RESTRUCTURING.getCode(),
                "E2E-FILE-" + UUID.randomUUID().toString().substring(0, 8),
                Map.of(),
                "TXN-E2E-" + UUID.randomUUID().toString().substring(0, 8),
                List.of(new LoanFacilityRestructuringMessage.InstallmentDetailsDTO(
                        1, new BigDecimal("10000"), new BigDecimal("500"), "2026-06-01")),
                12);

        byte[] payload = objectMapper.writeValueAsBytes(staleMessage);
        ProducerRecord<String, byte[]> record = KafkaTestHelper.buildRecord(
                FCB_EVENT_TOPIC,
                eventUid,
                payload,
                authToken,
                FcbEventOperationType.LOAN_FACILITY_RESTRUCTURING.getCode(),
                eventUid);

        // Stamp a v0 contract-version — below the consumer's minProducerVersion=1 → STALE_PRODUCER.
        record.headers()
                .add(new RecordHeader(MessagingHeaderNames.CONTRACT_VERSION, "0".getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader(MessagingHeaderNames.MIN_CONSUMER_VERSION, "1".getBytes(StandardCharsets.UTF_8)));

        long dltCountBefore = deadLetterRepository.count();

        sendAndWait(record);

        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertThat(deadLetterRepository.count()).isGreaterThan(dltCountBefore);
                    List<DeadLetterEntity> rows = deadLetterRepository.findAll().stream()
                            .filter(dl -> FCB_EVENT_TOPIC.equals(dl.getSourceTopic()))
                            .filter(dl -> dl.getErrorMessage() != null
                                    && dl.getErrorMessage().contains(VersionSkewDecision.STALE_PRODUCER.name()))
                            .toList();
                    assertThat(rows)
                            .as("expected a dead-letter row with STALE_PRODUCER for topic %s", FCB_EVENT_TOPIC)
                            .isNotEmpty();
                    DeadLetterEntity skew = rows.get(0);
                    assertThat(skew.getExceptionType()).contains("VersionSkewException");
                });

        // Handler must not have been invoked — the filter rejected the message before any business processing.
        verify(restructuringHandler, never()).handle(any());
    }
}
