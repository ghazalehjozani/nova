package ir.dotin.loan.trade.e2e.performance;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.context.Correlation;
import ir.dotin.platform.pangaea.commons.core.context.InvocationContext;
import ir.dotin.platform.pangaea.commons.core.context.InvocationContextHolder;
import ir.dotin.platform.pangaea.commons.domain.vo.NationalCode;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.ApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.CurrencyTypeDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.EconomicSectorDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.PartyDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SamatDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.e2e.orchestrator.MockPortConfigurator;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator.MinimalChain;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ConnectionPoolPinningE2ETest extends AbstractPerformanceE2E {

    private static final int POOL_SIZE = 2;
    private static final int CONCURRENT_DISPATCHES = 6;
    private static final long FCB_SLEEP_MILLIS = 2000L;
    private static final long MAX_WALL_MILLIS = 4500L;

    @Autowired
    private CommandDispatcher commandDispatcher;

    @Autowired
    private MockPortConfigurator mockPortConfigurator;

    @Autowired
    private PrerequisiteOrchestrator prerequisiteOrchestrator;

    @Autowired
    private DataSource dataSource;

    @SuppressWarnings("NullAway.Init")
    private String loanTypeCode;

    @SuppressWarnings("NullAway.Init")
    private String arrangementCode;

    @DynamicPropertySource
    static void constrainHikariPool(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> POOL_SIZE);
        registry.add("spring.datasource.hikari.minimum-idle", () -> POOL_SIZE);
        registry.add("spring.datasource.hikari.connection-timeout", () -> 3000);
    }

    @BeforeAll
    void setupFixtures() {
        mockPortConfigurator.configureAllDefaults();
        stubSlowCustomerInfo();

        MinimalChain chain = prerequisiteOrchestrator.createMinimalChain();
        TradeLoanArrangementEntity arrangement = chain.arrangement();
        TradeLoanTypeEntity loanType = chain.loanType();
        loanTypeCode = requireNonNull(
                requireNonNull(loanType.getCode(), "loan type code after save").getValue(),
                "loan type code value after save");
        arrangementCode = requireNonNull(arrangement.getCode(), "arrangement code after save");
    }

    @Test
    void txFreeFcbCall_doesNotPinPooledConnections_underConstrainedPool() throws Exception {
        AtomicInteger peakActive = new AtomicInteger(0);
        AtomicInteger peakPending = new AtomicInteger(0);
        Thread sampler = startPoolSampler(peakActive, peakPending);

        List<Future<Boolean>> futures = new ArrayList<>();
        Instant start = Instant.now();
        for (int i = 0; i < CONCURRENT_DISPATCHES; i++) {
            futures.add(executor.submit(dispatchOne()));
        }

        List<Throwable> failures = new ArrayList<>();
        int successes = 0;
        for (Future<Boolean> future : futures) {
            try {
                future.get();
                successes++;
            } catch (Exception e) {
                failures.add(rootCause(e));
            }
        }
        Duration wall = Duration.between(start, Instant.now());
        sampler.interrupt();
        sampler.join(Duration.ofSeconds(5).toMillis());

        PERF_LOG.info(
                "ConnectionPoolPinning: dispatches={}, successes={}, failures={}, wallMs={}, peakActive={}, peakPending={}",
                CONCURRENT_DISPATCHES,
                successes,
                failures.size(),
                wall.toMillis(),
                peakActive.get(),
                peakPending.get());

        assertThat(failures)
                .as("no dispatch may fail; connection-pool exhaustion would surface as SQLTransientConnectionException")
                .isEmpty();
        assertThat(successes).isEqualTo(CONCURRENT_DISPATCHES);

        assertThat(wall.toMillis())
                .as(
                        "tx-free FCB call => one ~%dms wave; pinned connections with pool=%d would force >=3 waves (~%dms+)",
                        FCB_SLEEP_MILLIS, POOL_SIZE, 3 * FCB_SLEEP_MILLIS)
                .isLessThan(MAX_WALL_MILLIS);

        assertThat(peakActive.get())
                .as("active pooled connections must never exceed the pool ceiling")
                .isLessThanOrEqualTo(POOL_SIZE);
        assertThat(peakPending.get())
                .as("no thread should be parked waiting for a pooled connection during the slow FCB call")
                .isZero();
    }

    private Callable<Boolean> dispatchOne() {
        return () -> {
            UUID uid = UUID.randomUUID();
            InvocationContext context = InvocationContext.builder()
                    .correlation(new Correlation(uid.toString(), null, uid, Instant.now(), null, null))
                    .build();
            return InvocationContextHolder.call(context, () -> {
                commandDispatcher.dispatch(buildCommand(uid));
                return Boolean.TRUE;
            });
        };
    }

    private Thread startPoolSampler(AtomicInteger peakActive, AtomicInteger peakPending) {
        HikariPoolMXBean pool = ((HikariDataSource) dataSource).getHikariPoolMXBean();
        Thread sampler = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                peakActive.accumulateAndGet(pool.getActiveConnections(), Math::max);
                peakPending.accumulateAndGet(pool.getThreadsAwaitingConnection(), Math::max);
                try {
                    Thread.sleep(25L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        sampler.setDaemon(true);
        sampler.setName("hikari-pool-sampler");
        sampler.start();
        return sampler;
    }

    private void stubSlowCustomerInfo() {
        PartyInfoResponse payload = new PartyInfoResponse(
                new ApplicantParty("12345678", PartyType.REAL, new CustomerName("Test", "User", "")),
                new NationalCode("1234567890"),
                false,
                false,
                false,
                true);
        when(customerServicePort.loadCustomerInfo(any(), any(), any(), any())).thenAnswer(invocation -> {
            Thread.sleep(FCB_SLEEP_MILLIS);
            return Result.success(payload);
        });
    }

    private OriginateLoanFacilityCommand buildCommand(UUID uid) {
        return OriginateLoanFacilityCommand.builder()
                .uid(uid)
                .version(null)
                .loanTypeCode(loanTypeCode)
                .loanArrangementCode(arrangementCode)
                .loanApplication(OriginateLoanFacilityCommand.LoanApplicationDto.builder()
                        .requestDate(Instant.now())
                        .parties(Set.of(new PartyDto.ApplicantDto("12345678")))
                        .requestedAmount(new AmountDto(new BigDecimal("50000000")))
                        .currency(new CurrencyTypeDto("IRR"))
                        .requestedLoanDuration(new OriginateLoanFacilityCommand.LoanDurationDto(Period.ofMonths(12)))
                        .applicantChannel(ApplicantChannel.DIGITAL_BANK)
                        .gracePeriod(new OriginateLoanFacilityCommand.GracePeriodDto(Period.ofDays(10)))
                        .installmentCount(new OriginateLoanFacilityCommand.InstallmentCountDto(3))
                        .disburseDestination(new DisburseDestinationDto.DepositDestinationDto("1.10.1357.60"))
                        .economicSector(new EconomicSectorDto("2-1"))
                        .branch(new OriginateLoanFacilityCommand.BranchDto("1"))
                        .requestReason(new OriginateLoanFacilityCommand.RequestReasonDto("0"))
                        .subSource(new OriginateLoanFacilityCommand.SubSourceDto("03"))
                        .description(new OriginateLoanFacilityCommand.DescriptionDto("connection-pool-pinning E2E"))
                        .disbursementMethod(DisbursementMethod.IRREGULAR_PROGRESSIVE)
                        .samat(new SamatDto("1234567899876543", null, null, null, null, null))
                        .applicationNumber(null)
                        .credibilityRank(null)
                        .build())
                .installmentSchedulePlan(null)
                .build();
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
