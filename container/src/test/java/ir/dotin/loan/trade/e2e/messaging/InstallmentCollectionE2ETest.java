package ir.dotin.loan.trade.e2e.messaging;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentOperationType;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentPaymentMessage;
import ir.dotin.loan.trade.e2e.AbstractMessagingE2E;
import ir.dotin.loan.trade.e2e.fixture.FormulaTestFixture;
import ir.dotin.loan.trade.e2e.fixture.KafkaTestHelper;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture.DisbursedFacilityResult;
import ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture;
import ir.dotin.loan.trade.e2e.orchestrator.MockPortConfigurator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class InstallmentCollectionE2ETest extends AbstractMessagingE2E {

    private static final String INSTALLMENT_OPERATION_TOPIC =
            "corridor.core.loan.nova.installment-operation.request.queue.v1";
    private static final String RESPONSE_TOPIC = "corridor.core.loan.nova.installment-operation.response.queue.v1.e2e";

    @Autowired
    private FormulaTestFixture formulaFixture;

    @Autowired
    private LoanArrangementTestFixture arrangementFixture;

    @Autowired
    private LoanTypeTestFixture loanTypeFixture;

    @Autowired
    private LoanFacilityTestFixture facilityFixture;

    @Autowired
    private MockPortConfigurator mockPortConfigurator;

    @Value("${platform.messaging.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private DisbursedFacilityResult facilityResult;
    private KafkaConsumer<String, byte[]> responseConsumer;

    @BeforeAll
    void setupFixtures() {
        formulaFixture.createDefaultFormulas();
        TradeLoanArrangementEntity arrangement = arrangementFixture.createDefaultArrangement();
        TradeLoanTypeEntity loanType = loanTypeFixture.createDefaultLoanType(arrangement.getId());

        facilityResult = facilityFixture.createDisbursedFacilityForCollection(
                loanType.getId(), loanType.getCode().getValue(), arrangement.getId());

        responseConsumer = KafkaTestHelper.createResponseConsumer(
                bootstrapServers,
                RESPONSE_TOPIC,
                "e2e-response-consumer-" + UUID.randomUUID().toString().substring(0, 8));
    }

    @BeforeEach
    void setupMocks() {
        mockPortConfigurator.configureAllDefaults();
    }

    @AfterAll
    void cleanupConsumer() {
        if (responseConsumer != null) {
            responseConsumer.close();
        }
    }

    @Test
    void shouldCollectInstallment_andSendSuccessResponse() throws Exception {
        String eventUid = UUID.randomUUID().toString();

        InstallmentPaymentMessage message = new InstallmentPaymentMessage(
                "E2E_PRODUCER",
                eventUid,
                new Date(),
                1,
                RESPONSE_TOPIC,
                null,
                InstallmentOperationType.INSTALLMENT_COLLECTION.getCode(),
                facilityResult.applicationNumber(),
                "TXN-E2E-" + UUID.randomUUID().toString().substring(0, 8),
                null,
                null,
                "IRR",
                new BigDecimal("33333333"),
                new BigDecimal("5000000"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                List.of(new InstallmentPaymentMessage.PaymentDetailDto(
                        1,
                        new BigDecimal("33333333"),
                        new BigDecimal("5000000"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("38333333"),
                        null,
                        null,
                        true)),
                Map.of());

        byte[] payload = objectMapper.writeValueAsBytes(message);
        var record = KafkaTestHelper.buildRecord(
                INSTALLMENT_OPERATION_TOPIC,
                eventUid,
                payload,
                authToken,
                InstallmentOperationType.INSTALLMENT_COLLECTION.getCode(),
                eventUid);

        sendAndWait(record);

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    ConsumerRecord<String, byte[]> response =
                            KafkaTestHelper.awaitResponse(responseConsumer, eventUid, Duration.ofSeconds(2));
                    assertThat(response).isNotNull();

                    JsonNode responseBody = objectMapper.readTree(response.value());
                    assertThat(responseBody.get("status").asText()).isEqualTo("SUCCESS");
                    assertThat(responseBody.get("eventUid").asText()).isEqualTo(eventUid);
                    assertThat(responseBody.get("fileNumber").asText()).isEqualTo(facilityResult.applicationNumber());
                });
    }

    @Test
    void shouldReturnFailedResponse_forUnknownFileNumber() throws Exception {
        String eventUid = UUID.randomUUID().toString();

        InstallmentPaymentMessage message = new InstallmentPaymentMessage(
                "E2E_PRODUCER",
                eventUid,
                new Date(),
                1,
                RESPONSE_TOPIC,
                null,
                InstallmentOperationType.INSTALLMENT_COLLECTION.getCode(),
                "NON-EXISTENT-FILE-NUMBER",
                "TXN-FAIL-" + UUID.randomUUID().toString().substring(0, 8),
                null,
                null,
                "IRR",
                new BigDecimal("10000000"),
                new BigDecimal("500000"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                List.of(new InstallmentPaymentMessage.PaymentDetailDto(
                        1,
                        new BigDecimal("10000000"),
                        new BigDecimal("500000"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("10500000"),
                        null,
                        null,
                        true)),
                Map.of());

        byte[] payload = objectMapper.writeValueAsBytes(message);
        var record = KafkaTestHelper.buildRecord(
                INSTALLMENT_OPERATION_TOPIC,
                eventUid,
                payload,
                authToken,
                InstallmentOperationType.INSTALLMENT_COLLECTION.getCode(),
                eventUid);

        sendAndWait(record);

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    ConsumerRecord<String, byte[]> response =
                            KafkaTestHelper.awaitResponse(responseConsumer, eventUid, Duration.ofSeconds(2));
                    assertThat(response).isNotNull();

                    JsonNode responseBody = objectMapper.readTree(response.value());
                    assertThat(responseBody.get("status").asText()).isEqualTo("FAILED");
                    assertThat(responseBody.get("eventUid").asText()).isEqualTo(eventUid);
                });
    }
}
