package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ValidateFacilityStepTest {

    @Mock
    private FacilityDependencyLoader dependencyLoader;

    @Mock
    private TradeLoanFacility facility;

    @Mock
    private TradeSanctionedLoan sanctionedLoan;

    @InjectMocks
    private ValidateFacilityStep step;

    @Test
    void executeFailsWhenFacilityMissing() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(dependencyLoader.loadFacility(LoanFacilityId.of(facilityId)))
                .thenReturn(Result.failure(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, facilityId)));

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void executeFailsWhenDisbursementMethodIsNotLumpSum() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(dependencyLoader.loadFacility(LoanFacilityId.of(facilityId))).thenReturn(Result.success(facility));
        when(facility.getSanctionedLoan()).thenReturn(Optional.of(sanctionedLoan));
        when(sanctionedLoan.getDisbursementMethod()).thenReturn(DisbursementMethod.IRREGULAR_PROGRESSIVE);

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
    }

    private static LumpSumData data(UUID facilityId) {
        return LumpSumData.initial(
                facilityId, "001", TransactionConfig.builder().build(), LocalDate.of(2026, 1, 1), 0L);
    }
}
