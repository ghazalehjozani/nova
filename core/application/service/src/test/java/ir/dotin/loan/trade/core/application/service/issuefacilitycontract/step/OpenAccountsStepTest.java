package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.IssueContractCommitmentHandlingStrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class OpenAccountsStepTest {

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private TradeLoanTypeRepository loanTypeRepository;

    @Mock
    private TradeLoanArrangementRepository loanArrangementRepository;

    @Mock
    private IssueContractCommitmentHandlingStrategy issueContractStrategy;

    @Mock
    private LoanTopicResolver loanTopicResolver;

    @Mock
    private AccountResolutionService accountResolutionService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanFacility facility;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanType loanType;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanArrangement arrangement;

    @InjectMocks
    private OpenAccountsStep step;

    @Test
    void executeResolvesAndStoresAccounts() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(facilityRepository.findById(LoanFacilityId.of(facilityId))).thenReturn(Optional.of(facility));
        when(loanTypeRepository.findById(any())).thenReturn(Optional.of(loanType));
        when(loanArrangementRepository.findById(any())).thenReturn(Optional.of(arrangement));
        when(issueContractStrategy.getRequiredRelationTypes()).thenReturn(Set.of());
        when(loanTopicResolver.resolveTopics(any(), any(), anySet())).thenReturn(Set.<LoanTopic>of());
        when(arrangement.getCurrencyType().getCode()).thenReturn("IRR");
        when(accountResolutionService.resolveAccounts(anySet(), any(), any()))
                .thenReturn(Result.success(new ResolvedAccounts(Map.of())));

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        assertThat(ctx.data().resolvedAccounts()).isEmpty();
    }

    @Test
    void compensateClosesOpenedAccounts() {
        UUID facilityId = UUID.randomUUID();
        ContractData seeded = data(facilityId).withResolvedAccounts(Map.of("PRINCIPAL", "1234567890"));
        WorkflowContextStub ctx = new WorkflowContextStub(seeded);

        when(accountResolutionService.closeAccounts(any())).thenReturn(Result.success(List.<AccountNumber>of()));

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
    }

    private static ContractData data(UUID facilityId) {
        return ContractData.initial(
                facilityId, "001", TransactionConfig.builder().build(), 0L);
    }
}
