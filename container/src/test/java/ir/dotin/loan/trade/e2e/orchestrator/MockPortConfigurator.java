package ir.dotin.loan.trade.e2e.orchestrator;

import java.util.List;

import org.springframework.boot.test.context.TestComponent;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.NationalCode;
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
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByIdPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindOrCreateAccountPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
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

import lombok.RequiredArgsConstructor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@TestComponent
@RequiredArgsConstructor
public class MockPortConfigurator {

    private final LoanServicePort loanServicePort;
    private final AccountServicePort accountServicePort;
    private final TransactionPostingPort transactionPostingPort;
    private final CollateralServicePort collateralServicePort;
    private final DepositServicePort depositServicePort;
    private final CustomerServicePort customerServicePort;
    private final FindOrCreateAccountPort findOrCreateAccountPort;
    private final FindAccountByIdPort findAccountByIdPort;
    private final FetchSanctionDetailsPort fetchSanctionDetailsPort;

    public void configureAllDefaults() {
        configureLoanServiceDefaults();
        configureAccountServiceDefaults();
        configureTransactionPostingDefaults();
        configureCollateralServiceDefaults();
        configureDepositServiceDefaults();
        configureCustomerServiceDefaults();
        configureFindOrCreateAccountDefaults();
        configureFetchSanctionDefaults();
    }

    public void resetAll() {
        reset(
                loanServicePort,
                accountServicePort,
                transactionPostingPort,
                collateralServicePort,
                depositServicePort,
                customerServicePort,
                findOrCreateAccountPort,
                findAccountByIdPort,
                fetchSanctionDetailsPort);
    }

    private void configureLoanServiceDefaults() {
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
    }

    private void configureAccountServiceDefaults() {
        when(accountServicePort.openAccount(any(LoanTopic.class), anyString())).thenAnswer(invocation -> {
            LoanTopic topic = invocation.getArgument(0);
            return Result.success(new AccountInfo(new AccountId("ACC-" + topic.code()), topic));
        });
        when(accountServicePort.openAccount(any(CreateAccountInfo.class)))
                .thenReturn(Result.success(new AccountId("ACC-E2E-001")));
        when(accountServicePort.validateAccountNumber(any()))
                .thenReturn(Result.success(new AccountNumber("1.10.1357.60")));
        when(findAccountByIdPort.findAccountById(any())).thenAnswer(invocation -> {
            AccountId id = invocation.getArgument(0);
            return Result.failure(ir.dotin.platform.pangaea.commons.core.Notification.ofError(
                    ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors
                            .KAFKA_INVALID_RESPONSE,
                    "findAccountById-stub"));
        });
    }

    private void configureTransactionPostingDefaults() {
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
    }

    private void configureCollateralServiceDefaults() {
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
    }

    private void configureDepositServiceDefaults() {
        when(depositServicePort.getDepositInfo(any()))
                .thenReturn(Result.success(new ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo(
                        new DepositNumber("1.10.1357.60"),
                        "E2E Test Deposit",
                        "CURRENT",
                        new CurrencyType(java.util.Currency.getInstance("IRR")),
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
    }

    private void configureCustomerServiceDefaults() {
        when(customerServicePort.loadCustomerInfo(any(), any(), any(), any()))
                .thenReturn(Result.success(new PartyInfoResponse(
                        new ApplicantParty("12345678", PartyType.REAL, new CustomerName("Test", "User", "")),
                        new NationalCode("1234567890"),
                        false,
                        false,
                        false,
                        true)));
        when(customerServicePort.findRelatedCustomers(any())).thenReturn(Result.success(List.of()));
    }

    private void configureFindOrCreateAccountDefaults() {
        when(findOrCreateAccountPort.findOrCreateAccount(any())).thenAnswer(invocation -> {
            LoanTopic topic = invocation.getArgument(0);
            return Result.success(new AccountInfo(new AccountId("ACC-E2E-" + topic.code()), topic));
        });
    }

    private void configureFetchSanctionDefaults() {
        when(fetchSanctionDetailsPort.fetchBySanctionSerial(any()))
                .thenReturn(Result.success(
                        new ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails(
                                "E2E-SANCTION",
                                ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType.GENERAL,
                                java.math.BigDecimal.ZERO,
                                new CurrencyType(java.util.Currency.getInstance("IRR")),
                                java.time.Period.ZERO,
                                0,
                                java.time.Period.ZERO,
                                ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod.LUMP_SUM,
                                null,
                                null,
                                null,
                                new ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType("1"))));
    }
}
