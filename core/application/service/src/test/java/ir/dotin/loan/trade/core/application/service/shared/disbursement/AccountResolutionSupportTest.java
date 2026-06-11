package ir.dotin.loan.trade.core.application.service.shared.disbursement;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.DisbursementStrategyProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class AccountResolutionSupportTest {

    @Mock
    private DisbursementStrategyProvider strategyProvider;

    @Mock
    private LoanTopicResolver loanTopicResolver;

    @Mock
    private AccountResolutionService accountResolutionService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanFacility facility;

    @Mock
    private ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType loanType;

    @InjectMocks
    private AccountResolutionSupport support;

    @Test
    void resolveAccountsSerializesResolvedMap() {
        RelationType<?> relationType = TradeRelationType.PRINCIPAL;
        ResolvedAccounts resolved = new ResolvedAccounts(Map.of(relationType, new AccountId("1234")));

        when(strategyProvider.getAllRequiredRelationTypes(facility)).thenReturn(Set.of(TradeRelationType.PRINCIPAL));
        when(loanTopicResolver.resolveTopics(any(), any(), any())).thenReturn(Set.<LoanTopic>of());
        when(accountResolutionService.resolveAccounts(any(), any(), any())).thenReturn(Result.success(resolved));

        Result<Map<String, String>> result = support.resolveAccounts(facility, loanType, "IRR");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap()).containsEntry("PRINCIPAL", "1234");
    }

    @Test
    void closeAccountsSkipsWhenEmpty() {
        support.closeAccounts(Map.of());

        verifyNoInteractions(accountResolutionService);
    }

    @Test
    void closeAccountsDelegatesParsableNumbers() {
        AccountNumber number = AccountNumber.of("0123456789").unwrap();
        when(accountResolutionService.closeAccounts(anyList())).thenReturn(Result.success(List.of(number)));

        support.closeAccounts(Map.of("BORROWER", "0123456789"));

        verify(accountResolutionService).closeAccounts(anyList());
    }
}
