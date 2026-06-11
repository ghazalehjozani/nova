package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ValidateFacilityStepTest {

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private TradeLoanFacility facility;

    @InjectMocks
    private ValidateFacilityStep step;

    @Test
    void executeSucceedsWhenFacilityValidates() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(facilityRepository.findById(LoanFacilityId.of(facilityId))).thenReturn(Optional.of(facility));
        when(facility.validateIssueContract()).thenReturn(Result.success());

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void executeFailsWhenFacilityMissing() {
        UUID facilityId = UUID.randomUUID();
        WorkflowContextStub ctx = new WorkflowContextStub(data(facilityId));

        when(facilityRepository.findById(LoanFacilityId.of(facilityId))).thenReturn(Optional.empty());

        StepResult<Void> result = step.execute(ctx);

        assertThat(result.isFailure()).isTrue();
    }

    private static ContractData data(UUID facilityId) {
        return ContractData.initial(
                facilityId, "001", TransactionConfig.builder().build(), 0L);
    }
}
