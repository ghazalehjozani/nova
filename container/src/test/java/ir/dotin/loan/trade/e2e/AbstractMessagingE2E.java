package ir.dotin.loan.trade.e2e;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Import;

import ir.dotin.loan.trade.Main;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByIdPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindOrCreateAccountPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;

@SpringBootTest(
        classes = {Main.class, E2ETestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Import({
    ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture.class,
    ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture.class,
    ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractMessagingE2E {

    @MockitoBean
    protected LoanServicePort loanServicePort;

    @MockitoBean
    protected AccountServicePort accountServicePort;

    @MockitoBean
    protected TransactionPostingPort transactionPostingPort;

    @MockitoBean
    protected CollateralServicePort collateralServicePort;

    @MockitoBean
    protected DepositServicePort depositServicePort;

    @MockitoBean
    protected CustomerServicePort customerServicePort;

    @MockitoBean
    protected FindOrCreateAccountPort findOrCreateAccountPort;

    @MockitoBean
    protected FindAccountByIdPort findAccountByIdPort;

    @MockitoBean
    protected FetchSanctionDetailsPort fetchSanctionDetailsPort;

    @Autowired
    protected KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${e2e.auth.token:}")
    protected String authToken;

    protected ProducerRecord<String, byte[]> buildRecord(
            String topic, String key, Object payload) throws Exception {
        byte[] value = objectMapper.writeValueAsBytes(payload);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, value);

        String idempotencyKey = UUID.randomUUID().toString();
        record.headers()
                .add(new RecordHeader("Idempotency-Key",
                        idempotencyKey.getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("X-Request-DateTime",
                        Instant.now().toString().getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("Accept-Language",
                        "fa".getBytes(StandardCharsets.UTF_8)));

        if (authToken != null && !authToken.isBlank()) {
            record.headers().add(new RecordHeader("Authorization",
                    authToken.getBytes(StandardCharsets.UTF_8)));
        }

        record.headers()
                .add(new RecordHeader("traceparent",
                        ("00-" + UUID.randomUUID().toString().replace("-", "")
                                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                                + "-01").getBytes(StandardCharsets.UTF_8)))
                .add(new RecordHeader("tracestate",
                        "e2e=test".getBytes(StandardCharsets.UTF_8)));

        return record;
    }

    protected void sendAndWait(ProducerRecord<String, byte[]> record)
            throws ExecutionException, InterruptedException, TimeoutException {
        kafkaTemplate.send(record).get(30, TimeUnit.SECONDS);
    }
}
