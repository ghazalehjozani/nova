package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.ApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.TradeIssueContractTransactionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class PostTransactionStepTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-11T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private TradeLoanTypeRepository loanTypeRepository;

    @Mock
    private TradeIssueContractTransactionService transactionService;

    @Mock
    private TransactionPostingPort transactionPostingPort;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IssueFacilityContractConfiguration configuration;

    @Mock
    private TradeLoanFacility facility;

    @Mock
    private TradeLoanApplication application;

    @Mock
    private ApplicationNumber applicationNumber;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanType loanType;

    @Mock
    private LoanTransaction transaction;

    private PostTransactionStep step() {
        return new PostTransactionStep(
                facilityRepository,
                loanTypeRepository,
                transactionService,
                transactionPostingPort,
                configuration,
                clock);
    }

    @Test
    void executePostsTransactionAndStoresTrackingData() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(facilityRepository.findById(LoanFacilityId.of(facilityId))).thenReturn(Optional.of(facility));
        when(loanTypeRepository.findById(any())).thenReturn(Optional.of(loanType));
        when(configuration.getPostTitleTemplate()).thenReturn("Issue Contract - Facility: %s");
        when(facility.getId()).thenReturn(LoanFacilityId.of(facilityId));
        when(facility.getLoanApplication()).thenReturn(application);
        when(loanType.getCode().value()).thenReturn("LT-1");
        when(applicationNumber.formattedApplicationNumber()).thenReturn("APP-1");
        when(application.getApplicationNumber()).thenReturn(Optional.of(applicationNumber));
        ApplicantParty applicant =
                new ApplicantParty("CUST-1", PartyType.REAL, new CustomerName("Ada", "Lovelace", null));
        when(application.getApplicant()).thenReturn(applicant);
        when(transactionService.createIssueContractTransaction(any(), any(), any(), any(), any(), any()))
                .thenReturn(Result.success(transaction));

        TrackedTransactionNumber tracked =
                TrackedTransactionNumber.create("100200300", TransactionStatus.POSTED, clock);
        when(transactionPostingPort.postTransaction(transaction)).thenReturn(Result.success(tracked));

        StepResult<Void> result = step().execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        assertThat(ctx.data().postedTransactionNumber()).isEqualTo("100200300");
        verify(transactionPostingPort).postTransaction(transaction);
    }

    @Test
    void compensateReversesPostedTransaction() {
        UUID facilityId = UUID.randomUUID();
        ContractData seeded = data(facilityId)
                .withPostedTransaction("100200300", "track-1", TransactionStatus.POSTED, clock.instant());
        WorkflowContextStub ctx = new WorkflowContextStub(seeded);

        when(transactionPostingPort.reverseTransaction(any())).thenReturn(Result.success(Unit.INSTANCE));

        StepResult<Void> result = step().compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(transactionPostingPort).reverseTransaction(any());
    }

    private static ContractData data(UUID facilityId) {
        TransactionConfig config = TransactionConfig.builder()
                .userId("user-1")
                .branchCode("001")
                .terminalId("term-1")
                .terminalIp("10.0.0.1")
                .terminalType("POS")
                .channel("BRANCH")
                .toolSource("CORE")
                .productCode("PROD-1")
                .networkType("INTERNAL")
                .build();
        return ContractData.initial(facilityId, "001", config, 0L);
    }
}
