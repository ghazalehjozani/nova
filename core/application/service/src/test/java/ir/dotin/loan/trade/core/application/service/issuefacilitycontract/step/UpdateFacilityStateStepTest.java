package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class UpdateFacilityStateStepTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-11T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock
    private TradeLoanFacility facility;

    private UpdateFacilityStateStep step() {
        return new UpdateFacilityStateStep(facilityRepository, clock);
    }

    @Test
    void executeIssuesContractAndSavesUnderExpectedVersion() {
        UUID facilityId = UUID.randomUUID();
        ContractData seeded = postedData(facilityId, 7L);
        WorkflowContextStub ctx = new WorkflowContextStub(seeded);

        when(facilityRepository.findById(LoanFacilityId.of(facilityId))).thenReturn(Optional.of(facility));
        when(facility.issueContract(any(), any(), eq(clock))).thenReturn(Result.success(Unit.INSTANCE));
        when(facility.domainEvents()).thenReturn(List.<DomainEvent<?>>of());

        StepResult<List<DomainEvent<?>>> result = step().execute(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facilityRepository).save(facility, 7L);
    }

    @Test
    void compensateRevertsContractIssuance() {
        UUID facilityId = UUID.randomUUID();
        ContractData seeded = postedData(facilityId, 7L);
        WorkflowContextStub ctx = new WorkflowContextStub(seeded);

        TrackedTransactionNumber reversed =
                TrackedTransactionNumber.create("100200300", TransactionStatus.REVERSED, clock);
        when(facilityRepository.findById(LoanFacilityId.of(facilityId))).thenReturn(Optional.of(facility));
        when(facility.revertContractIssuance(clock)).thenReturn(Result.success(reversed));

        StepResult<Void> result = step().compensate(ctx);

        assertThat(result.isSuccess()).isTrue();
        verify(facilityRepository).save(facility);
    }

    private static ContractData postedData(UUID facilityId, long version) {
        return ContractData.initial(
                        facilityId, "001", TransactionConfig.builder().build(), version)
                .withPostedTransaction(
                        "100200300", "track-1", TransactionStatus.POSTED, Instant.parse("2026-06-11T00:00:00Z"));
    }
}
