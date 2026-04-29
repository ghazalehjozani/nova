package ir.dotin.loan.trade.e2e.performance;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.e2e.fixture.FormulaTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture;
import ir.dotin.loan.trade.e2e.fixture.builder.FullLifecycleMessageBuilder;
import ir.dotin.loan.trade.e2e.orchestrator.MockPortConfigurator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(MockPortConfigurator.class)
class FullLifecycleThroughputPerfE2ETest extends AbstractPerformanceE2E {

    private static final String FULL_LIFECYCLE_TOPIC = "corridor.core.loan.nova.full-lifecycle.request.queue.v1";

    @Autowired
    private FormulaTestFixture formulaFixture;

    @Autowired
    private LoanArrangementTestFixture arrangementFixture;

    @Autowired
    private LoanTypeTestFixture loanTypeFixture;

    @Autowired
    private TradeLoanFacilityJpaRepository facilityJpaRepository;

    @Autowired
    private MockPortConfigurator mockPortConfigurator;

    private TradeLoanArrangementEntity arrangement;
    private TradeLoanTypeEntity loanType;

    @BeforeAll
    void setupFixtures() {
        formulaFixture.createDefaultFormulas();
        arrangement = arrangementFixture.createDefaultArrangement();
        loanType = loanTypeFixture.createDefaultLoanType(arrangement.getId());
    }

    @BeforeEach
    void setupMocks() {
        mockPortConfigurator.configureAllDefaults();
    }

    @Test
    void shouldProcessMultipleConcurrentLifecycles() throws Exception {
        int concurrentRequests = 20;
        long countBefore = facilityJpaRepository.count();
        AtomicLong sent = new AtomicLong(0);

        PerfResult result = measureThroughput(concurrentRequests, () -> {
            FullLoanFacilityLifecycleMessage message = FullLifecycleMessageBuilder.defaults()
                    .withLoanTypeCode(loanType.getCode().getValue())
                    .withLoanArrangementCode(arrangement.getCode())
                    .withCustomerNumber("12345678")
                    .build();

            ProducerRecord<String, byte[]> record =
                    buildRecord(FULL_LIFECYCLE_TOPIC, UUID.randomUUID().toString(), message);
            sendAndWait(record);
            sent.incrementAndGet();
            return true;
        });

        PERF_LOG.info("Sent {} concurrent lifecycle messages", sent.get());

        // Wait for all facilities to be created (with generous timeout)
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    long countAfter = facilityJpaRepository.count();
                    PERF_LOG.info(
                            "Facilities created so far: {} (expected: {} new)",
                            countAfter - countBefore,
                            result.successes());
                    assertThat(countAfter).isGreaterThanOrEqualTo(countBefore + result.successes());
                });

        assertThat(result.successes())
                .as("At least some messages should be sent successfully")
                .isGreaterThan(0);
    }

    @Test
    void shouldMaintainThroughputUnderSustainedLoad() throws Exception {
        int batchSize = 10;
        int batchCount = 5;
        long countBefore = facilityJpaRepository.count();

        PerfResult result = measureSequentialBatch(batchSize, batchCount, () -> {
            FullLoanFacilityLifecycleMessage message = FullLifecycleMessageBuilder.defaults()
                    .withLoanTypeCode(loanType.getCode().getValue())
                    .withLoanArrangementCode(arrangement.getCode())
                    .withCustomerNumber("12345678")
                    .build();

            ProducerRecord<String, byte[]> record =
                    buildRecord(FULL_LIFECYCLE_TOPIC, UUID.randomUUID().toString(), message);
            sendAndWait(record);
            return true;
        });

        // Wait for facilities to be processed
        await().atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    long countAfter = facilityJpaRepository.count();
                    assertThat(countAfter).isGreaterThanOrEqualTo(countBefore + result.successes());
                });

        PERF_LOG.info(
                "Sustained load test: total={}, success={}, throughput={}/s",
                result.totalRequests(),
                result.successes(),
                String.format("%.2f", result.throughputPerSecond()));

        assertThat(result.successes()).isGreaterThan(0);
    }

    @Test
    void shouldHandleConcurrentLifecycleAndQueries() throws Exception {
        long countBefore = facilityJpaRepository.count();

        // Send lifecycle messages
        for (int i = 0; i < 10; i++) {
            FullLoanFacilityLifecycleMessage message = FullLifecycleMessageBuilder.defaults()
                    .withLoanTypeCode(loanType.getCode().getValue())
                    .withLoanArrangementCode(arrangement.getCode())
                    .withCustomerNumber("12345678")
                    .build();

            ProducerRecord<String, byte[]> record =
                    buildRecord(FULL_LIFECYCLE_TOPIC, UUID.randomUUID().toString(), message);
            sendAndWait(record);
        }

        // Concurrently query while messages are processing
        PerfResult queryResult = measureThroughput(20, () -> {
            // Direct repository query (simulates concurrent read load)
            facilityJpaRepository.count();
            return true;
        });

        assertThat(queryResult.failures()).isZero();

        // Wait for facilities to be created
        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    long countAfter = facilityJpaRepository.count();
                    assertThat(countAfter).isGreaterThan(countBefore);
                });
    }
}
