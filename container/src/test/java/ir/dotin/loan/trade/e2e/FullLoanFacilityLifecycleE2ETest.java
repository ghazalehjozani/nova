package ir.dotin.loan.trade.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.PartyRequestDto;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture;
import ir.dotin.platform.commons.core.Result;

class FullLoanFacilityLifecycleE2ETest extends AbstractMessagingE2E {

    private static final String FULL_LIFECYCLE_TOPIC =
            "corridor.core.loan.nova.full-lifecycle.request.queue.v1";

    @Autowired
    private LoanArrangementTestFixture arrangementFixture;

    @Autowired
    private LoanTypeTestFixture loanTypeFixture;

    @Autowired
    private TradeLoanFacilityJpaRepository facilityJpaRepository;

    private TradeLoanArrangementEntity arrangement;
    private TradeLoanTypeEntity loanType;

    @BeforeAll
    void setupFixtures() {
        arrangement = arrangementFixture.createDefaultArrangement();
        loanType = loanTypeFixture.createDefaultLoanType(arrangement.getId());
        configureMockStubs();
    }

    @SuppressWarnings("unchecked")
    private void configureMockStubs() {
        when(loanServicePort.loadEconomicalSectorByCode(any())).thenReturn(Result.success());
        when(loanServicePort.loadEconomicalSector(any())).thenReturn(Result.success());
        when(loanServicePort.validateEconomicalSectorForLoanType(any(), any())).thenReturn(Result.success());
        when(loanServicePort.loadReasonTypeForCreate(any())).thenReturn(Result.success());
        when(loanServicePort.loadResourceByCode(any())).thenReturn(Result.success());
        when(loanServicePort.loadTopicByCode(any())).thenReturn(Result.success(List.of()));
        when(loanServicePort.loadCoveredBranches(any())).thenReturn(Result.success(List.of()));
        when(loanServicePort.getApplicationNumber(any(), any(), any())).thenReturn(Result.success());
        when(loanServicePort.loadBranch(any())).thenReturn(Result.success());

        when(accountServicePort.openAccount(any(ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic.class)))
                .thenReturn(Result.success());
        when(accountServicePort.validateAccountNumber(any())).thenReturn(Result.success());

        when(transactionPostingPort.postTransaction(any())).thenReturn(Result.success());
        when(transactionPostingPort.postTransactions(any(), any(), any())).thenReturn(Result.success(List.of()));

        when(collateralServicePort.validateAddAssuranceToFile(any(), any(), any())).thenReturn(Result.success());

        when(depositServicePort.getDepositInfo(any())).thenReturn(Result.success());
        when(depositServicePort.validateDebtorDeposit(any(), any())).thenReturn(Result.success());

        when(customerServicePort.loadCustomerInfo(any(), any(), any(), any())).thenReturn(Result.success());
        when(customerServicePort.findRelatedCustomers(any())).thenReturn(Result.success(List.of()));

        when(findOrCreateAccountPort.findOrCreateAccount(any())).thenReturn(Result.success());

        when(fetchSanctionDetailsPort.fetchBySanctionSerial(any())).thenReturn(Result.success());
    }

    @Test
    void shouldProcessFullLifecycleSaga() throws Exception {
        String loanTypeCode = loanType.getCode().getValue();
        String arrangementCode = arrangement.getCode();

        FullLoanFacilityLifecycleMessage message = FullLoanFacilityLifecycleMessage.builder()
                .version(1L)
                .loanTypeCode(loanTypeCode)
                .loanArrangementCode(arrangementCode)
                .loanApplication(new FullLoanFacilityLifecycleMessage.LoanApplicationDto(
                        Instant.now(),
                        Set.of(new PartyRequestDto.ApplicantDto("12345678")),
                        new BigDecimal("50000000"),
                        "IRR",
                        12,
                        ApplicantChannel.DIGITAL_BANK,
                        10,
                        6,
                        new DisburseDestinationRequestDto.AccountDestinationDto("987-654-321"),
                        "2-1",
                        "1",
                        new FullLoanFacilityLifecycleMessage.RequestReasonDto("0"),
                        null,
                        "E2E test facility",
                        null,
                        DisbursementMethod.IRREGULAR_PROGRESSIVE,
                        null,
                        null))
                .disbursement(new FullLoanFacilityLifecycleMessage.DisbursementDto(
                        new BigDecimal("50000000"),
                        LocalDate.now()))
                .installmentSchedulePlan(new FullLoanFacilityLifecycleMessage.InstallmentSchedulePlanDto(
                        List.of(
                                new FullLoanFacilityLifecycleMessage.InstallmentSpecDto(
                                        1, Instant.now().plus(Duration.ofDays(30)),
                                        new BigDecimal("8333333"), new BigDecimal("750000")),
                                new FullLoanFacilityLifecycleMessage.InstallmentSpecDto(
                                        2, Instant.now().plus(Duration.ofDays(60)),
                                        new BigDecimal("8333333"), new BigDecimal("625000")),
                                new FullLoanFacilityLifecycleMessage.InstallmentSpecDto(
                                        3, Instant.now().plus(Duration.ofDays(90)),
                                        new BigDecimal("8333334"), new BigDecimal("500000")))))
                .collaterals(List.of())
                .confirmType("VERBAL")
                .metadata(Map.of())
                .build();

        ProducerRecord<String, byte[]> record = buildRecord(
                FULL_LIFECYCLE_TOPIC,
                UUID.randomUUID().toString(),
                message);

        long countBefore = facilityJpaRepository.count();

        sendAndWait(record);

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    long countAfter = facilityJpaRepository.count();
                    assertThat(countAfter).isGreaterThan(countBefore);
                });
    }

    @Test
    void shouldRejectInvalidLoanTypeCode() throws Exception {
        FullLoanFacilityLifecycleMessage message = FullLoanFacilityLifecycleMessage.builder()
                .version(1L)
                .loanTypeCode("NON_EXISTENT_TYPE_CODE")
                .loanArrangementCode(arrangement.getCode())
                .loanApplication(new FullLoanFacilityLifecycleMessage.LoanApplicationDto(
                        Instant.now(),
                        Set.of(new PartyRequestDto.ApplicantDto("99999999")),
                        new BigDecimal("50000000"),
                        "IRR",
                        12,
                        ApplicantChannel.INTERNET_BANK,
                        30,
                        6,
                        new DisburseDestinationRequestDto.AccountDestinationDto("111-222-333"),
                        "EXCHANGE",
                        "1",
                        new FullLoanFacilityLifecycleMessage.RequestReasonDto("01"),
                        null,
                        "E2E invalid type test",
                        null,
                        DisbursementMethod.LUMP_SUM,
                        null,
                        null))
                .disbursement(new FullLoanFacilityLifecycleMessage.DisbursementDto(
                        new BigDecimal("50000000"), LocalDate.now()))
                .installmentSchedulePlan(new FullLoanFacilityLifecycleMessage.InstallmentSchedulePlanDto(List.of()))
                .collaterals(List.of())
                .confirmType("VERBAL")
                .metadata(Map.of())
                .build();

        ProducerRecord<String, byte[]> record = buildRecord(
                FULL_LIFECYCLE_TOPIC,
                UUID.randomUUID().toString(),
                message);

        long countBefore = facilityJpaRepository.count();

        sendAndWait(record);

        // With invalid type code, no new facility should be created
        await().during(Duration.ofSeconds(5))
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    long countAfter = facilityJpaRepository.count();
                    assertThat(countAfter).isEqualTo(countBefore);
                });
    }
}
