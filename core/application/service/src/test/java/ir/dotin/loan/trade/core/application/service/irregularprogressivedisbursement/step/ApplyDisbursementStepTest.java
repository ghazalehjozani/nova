package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
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
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ApplyDisbursementStepTest {

    @Mock
    private FacilityDependencyLoader dependencyLoader;

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private Clock clock;

    @Mock
    private TradeLoanFacility facility;

    @InjectMocks
    private ApplyDisbursementStep step;

    @Test
    void executeFailsWhenFacilityMissing() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(dependencyLoader.loadFacility(LoanFacilityId.of(facilityId)))
                .thenReturn(Result.failure(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, facilityId)));

        StepResult<List<DomainEvent<?>>> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void compensateRevertsAndSavesFacility() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(dependencyLoader.loadFacility(LoanFacilityId.of(facilityId))).thenReturn(Result.success(facility));
        when(facility.revertIrregularTrancheDisbursement(clock))
                .thenReturn(Result.success(Optional.<TrackedTransactionNumber>empty()));

        StepResult<Void> result = step.compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facilityRepository).save(facility);
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
