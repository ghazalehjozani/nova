package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.AccountResolutionSupport;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ResolveAccountsStepTest {

    @Mock
    private FacilityDependencyLoader dependencyLoader;

    @Mock
    private AccountResolutionSupport accountResolutionSupport;

    @Mock
    private TradeLoanFacility facility;

    @Mock
    private TradeLoanType loanType;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanArrangement arrangement;

    @InjectMocks
    private ResolveAccountsStep step;

    @Test
    void executeStoresResolvedAccounts() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));
        Map<String, String> resolved = Map.of("BORROWER", "1234");

        when(dependencyLoader.loadFacility(LoanFacilityId.of(facilityId))).thenReturn(Result.success(facility));
        when(dependencyLoader.loadLoanType(facility)).thenReturn(Result.success(loanType));
        when(dependencyLoader.loadLoanArrangement(facility)).thenReturn(Result.success(arrangement));
        when(arrangement.getCurrencyType().getCode()).thenReturn("IRR");
        when(accountResolutionSupport.resolveAccounts(facility, loanType, "IRR"))
                .thenReturn(Result.success(resolved));

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        assertThat(ctx.data().resolvedAccounts()).isEqualTo(resolved);
    }

    @Test
    void compensateClosesResolvedAccounts() {
        UUID facilityId = UUID.randomUUID();
        Map<String, String> resolved = Map.of("BORROWER", "1234");
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId).withResolvedAccounts(resolved));

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(accountResolutionSupport).closeAccounts(resolved);
    }

    private static LumpSumData data(UUID facilityId) {
        return LumpSumData.initial(
                facilityId, "001", TransactionConfig.builder().build(), LocalDate.of(2026, 1, 1), 0L);
    }
}
