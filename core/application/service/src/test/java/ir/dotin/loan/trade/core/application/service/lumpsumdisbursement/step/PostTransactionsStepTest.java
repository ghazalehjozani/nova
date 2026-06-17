package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.TransactionPostingSupport;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class PostTransactionsStepTest {

    @Mock
    private FacilityDependencyLoader dependencyLoader;

    @Mock
    private TransactionPostingSupport transactionPostingSupport;

    @InjectMocks
    private PostTransactionsStep step;

    @Test
    void executeFailsWhenFacilityMissing() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(dependencyLoader.loadFacility(LoanFacilityId.of(facilityId)))
                .thenReturn(Result.failure(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, facilityId)));

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
        verifyNoInteractions(transactionPostingSupport);
    }

    @Test
    void compensateSucceedsWhenNoPostedTransactions() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void compensateReversesPostedTransactions() {
        UUID facilityId = UUID.randomUUID();
        LumpSumData seed = data(facilityId)
                .withPostedTransactions(
                        List.of(new LumpSumData.PostedTransactionData("TX-1", "TRK-1", TransactionStatus.POSTED)));
        WorkflowContextStub ctx = new WorkflowContextStub(seed);

        TrackedTransactionNumber tracked =
                TrackedTransactionNumber.create("TX-1", "TRK-1", TransactionStatus.POSTED, Clock.systemUTC());
        when(transactionPostingSupport.rebuildTrackedNumbers(anyList())).thenReturn(List.of(tracked));
        when(transactionPostingSupport.reverseTransactions(List.of(tracked))).thenReturn(Result.success(Unit.INSTANCE));

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(transactionPostingSupport).reverseTransactions(List.of(tracked));
    }

    private static LumpSumData data(UUID facilityId) {
        return LumpSumData.initial(
                facilityId, "001", TransactionConfig.builder().build(), LocalDate.of(2026, 1, 1), 0L);
    }
}
