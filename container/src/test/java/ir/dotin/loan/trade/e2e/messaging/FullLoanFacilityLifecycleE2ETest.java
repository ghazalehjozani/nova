package ir.dotin.loan.trade.e2e.messaging;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.ApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.PartyRequestDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.BranchDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CreditorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CurrencyValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DebtorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DepositClosedStatus;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.ReasonType;
import ir.dotin.loan.trade.e2e.AbstractMessagingE2E;
import ir.dotin.loan.trade.e2e.fixture.FormulaTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@Disabled("Requires Consul, Kafka, PostgreSQL and Redis infrastructure (run with -Pe2e profile)")
class FullLoanFacilityLifecycleE2ETest extends AbstractMessagingE2E {

    private static final String FULL_LIFECYCLE_TOPIC = "corridor.core.loan.nova.full-lifecycle.request.queue.v1";

    @Autowired
    private FormulaTestFixture formulaFixture;

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
        formulaFixture.createDefaultFormulas();
        arrangement = arrangementFixture.createDefaultArrangement();
        // persisted entity: generated id is non-null by contract after save
        loanType =
                loanTypeFixture.createDefaultLoanType(requireNonNull(arrangement.getId(), "arrangement id after save"));
    }

    @BeforeEach
    void setupMocks() {
        configureMockStubs();
    }

    private void configureMockStubs() {
        // LoanServicePort
        when(loanServicePort.loadEconomicalSectorByCode(any())).thenReturn(Result.success(new EconomicSector("2-1")));
        when(loanServicePort.loadEconomicalSector(any()))
                .thenReturn(Result.success(new EconomicalSectorResponse("2-1", "Exchange", false, "")));
        when(loanServicePort.validateEconomicalSectorForLoanType(any(), any()))
                .thenReturn(Result.success(new EconomicalSectorValidation(true, null)));
        when(loanServicePort.loadReasonTypeForCreate(any()))
                .thenReturn(Result.success(new ReasonType("0", "0", "Default reason", "CREATE", false, false)));
        when(loanServicePort.loadResourceByCode(any())).thenReturn(Result.success(new SubSource("03")));
        when(loanServicePort.loadTopicByCode(any())).thenReturn(Result.success(List.of()));
        doReturn(Result.success(List.of(new BranchCode("1"))))
                .when(loanServicePort)
                .loadCoveredBranches(any());
        when(loanServicePort.getApplicationNumber(any(), any(), any()))
                .thenReturn(ApplicationNumber.of(
                        new Branch(new BranchCode("1")),
                        new LoanTypeCode("LC001"),
                        ApplicantParty.of("CUST123", PartyType.REAL, new CustomerName("Mahdi", "Abdollahi", "Test"))
                                .unwrap(),
                        "1"));
        when(loanServicePort.loadBranch(any()))
                .thenReturn(Result.success(new BranchDetails(
                        "1", "Main Branch", "Main", 1L, "Manager", "1", "SWIFT", "001", "001", "001")));

        // AccountServicePort
        when(accountServicePort.openAccount(any(LoanTopic.class), anyString())).thenAnswer(invocation -> {
            LoanTopic topic = invocation.getArgument(0);
            return Result.success(new AccountInfo(new AccountId("ACC-" + topic.code()), topic));
        });
        when(accountServicePort.openAccount(any(CreateAccountInfo.class)))
                .thenReturn(Result.success(new AccountId("ACC-E2E-001")));
        when(accountServicePort.validateAccountNumber(any()))
                .thenReturn(Result.success(new AccountNumber("1.10.1357.60")));
        when(findAccountByIdPort.findAccountById(any()))
                .thenAnswer(invocation -> Result.failure(ir.dotin.platform.pangaea.commons.core.Notification.ofError(
                        ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors
                                .KAFKA_INVALID_RESPONSE,
                        "findAccountById-stub")));

        // TransactionPostingPort
        when(transactionPostingPort.postTransaction(any()))
                .thenReturn(Result.success(TrackedTransactionNumber.create(
                        "TXN-E2E-001", TransactionStatus.POSTED, java.time.Clock.systemUTC())));
        when(transactionPostingPort.postTransactions(any(), any(), any())).thenAnswer(invocation -> {
            List<Object> transactions = invocation.getArgument(2);
            List<TrackedTransactionNumber> results = new java.util.ArrayList<>();
            for (int i = 0; i < transactions.size(); i++) {
                results.add(TrackedTransactionNumber.create(
                        "TXN-E2E-" + (i + 1), TransactionStatus.POSTED, java.time.Clock.systemUTC()));
            }
            return Result.success(results);
        });
        when(transactionPostingPort.reverseTransaction(any())).thenReturn(Result.success());

        // CollateralServicePort
        when(collateralServicePort.validateAddAssuranceToFile(any(), any(), any()))
                .thenReturn(Result.success(
                        new ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation(
                                true, null)));
        when(collateralServicePort.reserveCollateral(any(), any(), any(), any(), any()))
                .thenReturn(Result.success(List.of()));
        when(collateralServicePort.loadCollateral(any(), any()))
                .thenReturn(Result.success(
                        new ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails(
                                "E2E-SERIAL",
                                "",
                                "",
                                "",
                                java.math.BigDecimal.ZERO,
                                java.math.BigDecimal.ZERO,
                                java.math.BigDecimal.ZERO,
                                0,
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                false,
                                false,
                                false,
                                false,
                                "")));
        when(collateralServicePort.unReserveCollateral(any(), any(), any(), any()))
                .thenReturn(Result.success(
                        new ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial("E2E-SERIAL")));

        // DepositServicePort
        when(depositServicePort.getDepositInfo(any()))
                .thenReturn(Result.success(new ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo(
                        new DepositNumber("1.10.1357.60"),
                        "E2E Test Deposit",
                        "CURRENT",
                        new ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType(
                                java.util.Currency.getInstance("IRR")),
                        "ACTIVE",
                        false,
                        "1",
                        List.of("12345678"))));
        when(depositServicePort.validateDebtorDeposit(any(), any()))
                .thenReturn(Result.success(new DebtorDepositValidation(true)));
        when(depositServicePort.validateCreditorDeposit(any(), any(), any()))
                .thenReturn(Result.success(new CreditorDepositValidation(true)));
        when(depositServicePort.hasDepositAllowedCurrencies(any(), any()))
                .thenReturn(Result.success(new CurrencyValidation(true, null)));
        when(depositServicePort.isDepositClosed(any(), any()))
                .thenReturn(Result.success(new DepositClosedStatus(false, "IRR")));
        when(depositServicePort.getAllDepositSignerOwnerCustomer(any())).thenReturn(Result.success(List.of()));

        // CustomerServicePort
        when(customerServicePort.loadCustomerInfo(any(), any(), any(), any()))
                .thenReturn(Result.success(new PartyInfoResponse(
                        new ApplicantParty(
                                "12345678",
                                ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType.REAL,
                                new CustomerName("Test", "User", "")),
                        new ir.dotin.platform.pangaea.commons.domain.vo.NationalCode("1234567890"),
                        false,
                        false,
                        false)));
        when(customerServicePort.findRelatedCustomers(any())).thenReturn(Result.success(List.of()));

        // FindOrCreateAccountPort
        when(findOrCreateAccountPort.findOrCreateAccount(any())).thenAnswer(invocation -> {
            LoanTopic topic = invocation.getArgument(0);
            return Result.success(new AccountInfo(new AccountId("ACC-E2E-" + topic.code()), topic));
        });

        // FetchSanctionDetailsPort
        when(fetchSanctionDetailsPort.fetchBySanctionSerial(any()))
                .thenReturn(Result.success(
                        new ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails(
                                "E2E-SANCTION",
                                ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType.GENERAL,
                                java.math.BigDecimal.ZERO,
                                new ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType(
                                        java.util.Currency.getInstance("IRR")),
                                java.time.Period.ZERO,
                                0,
                                java.time.Period.ZERO,
                                DisbursementMethod.LUMP_SUM,
                                null,
                                null,
                                null,
                                new ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType("1"))));
    }

    @Test
    void shouldProcessFullLifecycleSaga() throws Exception {
        // persisted fixtures: code is non-null by contract after save
        String loanTypeCode =
                requireNonNull(loanType.getCode(), "loan type code after save").getValue();
        String arrangementCode = requireNonNull(arrangement.getCode(), "arrangement code after save");

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
                        new DisburseDestinationRequestDto.DepositDestinationDto("1.10.1357.60"),
                        "2-1",
                        "1",
                        new FullLoanFacilityLifecycleMessage.RequestReasonDto("0"),
                        "03",
                        "E2E test facility",
                        null,
                        DisbursementMethod.IRREGULAR_PROGRESSIVE,
                        new FullLoanFacilityLifecycleMessage.SamatDto("1234567899876543", null, null, null, null, null),
                        "A"))
                .disbursement(new FullLoanFacilityLifecycleMessage.DisbursementDto(
                        new BigDecimal("50000000"), LocalDate.now()))
                .installmentSchedulePlan(new FullLoanFacilityLifecycleMessage.InstallmentSchedulePlanDto(List.of(
                        new FullLoanFacilityLifecycleMessage.InstallmentSpecDto(
                                1,
                                Instant.now().plus(Duration.ofDays(30)),
                                new BigDecimal("16666667"),
                                new BigDecimal("750000")),
                        new FullLoanFacilityLifecycleMessage.InstallmentSpecDto(
                                2,
                                Instant.now().plus(Duration.ofDays(60)),
                                new BigDecimal("16666667"),
                                new BigDecimal("625000")),
                        new FullLoanFacilityLifecycleMessage.InstallmentSpecDto(
                                3,
                                Instant.now().plus(Duration.ofDays(90)),
                                new BigDecimal("16666666"),
                                new BigDecimal("500000")))))
                .collaterals(List.of())
                .confirmType("1")
                .metadata(Map.of())
                .build();

        ProducerRecord<String, byte[]> record =
                buildRecord(FULL_LIFECYCLE_TOPIC, UUID.randomUUID().toString(), message);

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
                        new DisburseDestinationRequestDto.DepositDestinationDto("1.10.1357.60"),
                        "2-1",
                        "1",
                        new FullLoanFacilityLifecycleMessage.RequestReasonDto("0"),
                        "03",
                        "E2E invalid type test",
                        null,
                        DisbursementMethod.LUMP_SUM,
                        new FullLoanFacilityLifecycleMessage.SamatDto("1234567899876543", null, null, null, null, null),
                        "A"))
                .disbursement(new FullLoanFacilityLifecycleMessage.DisbursementDto(
                        new BigDecimal("50000000"), LocalDate.now()))
                .installmentSchedulePlan(new FullLoanFacilityLifecycleMessage.InstallmentSchedulePlanDto(
                        List.of(new FullLoanFacilityLifecycleMessage.InstallmentSpecDto(
                                1,
                                Instant.now().plus(Duration.ofDays(30)),
                                new BigDecimal("50000000"),
                                new BigDecimal("1000000")))))
                .collaterals(List.of())
                .confirmType("1")
                .metadata(Map.of())
                .build();

        ProducerRecord<String, byte[]> record =
                buildRecord(FULL_LIFECYCLE_TOPIC, UUID.randomUUID().toString(), message);

        long countBefore = facilityJpaRepository.count();

        sendAndWait(record);

        // With invalid type code, no new facility should be created
        await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            long countAfter = facilityJpaRepository.count();
            assertThat(countAfter).isEqualTo(countBefore);
        });
    }
}
