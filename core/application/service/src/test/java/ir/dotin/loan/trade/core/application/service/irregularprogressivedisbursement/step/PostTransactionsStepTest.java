package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.configuration.IrregularProgressiveDisbursementConfiguration;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData.PostedTransactionData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.TransactionPostingSupport;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.IrregularProgressiveDisbursementTransactionService;

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

    @Mock
    private IrregularPlanRecalculator planRecalculator;

    @Mock
    private IrregularProgressiveDisbursementTransactionService transactionService;

    @Mock
    private IrregularProgressiveDisbursementConfiguration configuration;

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
        IrregularDisbursementData seed = data(facilityId)
                .withPostedTransactions(
                        List.of(new PostedTransactionData("TX-1", "TRK-1", TransactionStatus.POSTED)), Map.of());
        WorkflowContextStub ctx = new WorkflowContextStub(seed);

        TrackedTransactionNumber tracked =
                TrackedTransactionNumber.create("TX-1", "TRK-1", TransactionStatus.POSTED, Clock.systemUTC());
        when(transactionPostingSupport.rebuildTrackedNumbers(anyList())).thenReturn(List.of(tracked));
        when(transactionPostingSupport.reverseTransactions(List.of(tracked))).thenReturn(Result.success(Unit.INSTANCE));

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(transactionPostingSupport).reverseTransactions(List.of(tracked));
    }

    private static IrregularDisbursementData data(UUID facilityId) {
        return IrregularDisbursementData.initial(
                facilityId,
                "001",
                TransactionConfig.builder().build(),
                LocalDate.of(2026, 1, 1),
                0L,
                BigDecimal.valueOf(1000),
                "IRR",
                1,
                null,
                List.of());
    }
}
