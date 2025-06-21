package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityContractIssuedEvent;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

import static ir.dotin.loan.trade.core.domain.ResultAssert.assertFailureHasErrors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@DisplayName("TradeLoanFacility Contract Issuance Tests")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"NullAway", "TimeZoneUsage"})
final class TradeLoanFacilityTest {

    private static final TradeLoanFacilityId FACILITY_ID = TradeLoanFacilityId.generate();
    private static final TradeLoanTypeId LOAN_TYPE_ID = TradeLoanTypeId.generate();
    private static final TradeLoanArrangementId LOAN_ARRANGEMENT_ID = TradeLoanArrangementId.generate();

    private TradeLoanFacility createFacility(
            FacilityStatus initialStatus, TradeLoanApplication loanApplication, TradeSanctionedLoan sanctionedLoan) {

        return TradeLoanFacility.reconstitute(
                FACILITY_ID, loanApplication, sanctionedLoan, initialStatus, LOAN_TYPE_ID, LOAN_ARRANGEMENT_ID);
    }

    @DisplayName("Contract Issuance Event Generation")
    @Nested
    final class ContractIssuanceEventGenerationTest {

        @DisplayName("should generate TradeLoanFacilityContractIssuedEvent with transaction numbers")
        @Test
        void shouldGenerateContractIssuedEventWithTransactionNumbers(
                @Mock TradeLoanApplication loanApplication, @Mock TradeSanctionedLoan sanctionedLoan) {
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> transactionNumbers = ImmutableList.of(
                    TransactionNumber.of("TXN-TRADE-001").orElseThrow(),
                    TransactionNumber.of("TXN-TRADE-002").orElseThrow());
            when(sanctionedLoan.getId()).thenReturn(TradeSanctionedLoanId.generate());

            TradeLoanFacility facility = createFacility(FacilityStatus.APPROVED, loanApplication, sanctionedLoan);

            Result<Void> result = facility.issueContract(transactionNumbers, clock);

            assertThat(result.isSuccess()).isTrue();
            assertThat(facility.domainEvents()).hasSize(1);

            DomainEvent<?, ?> event = facility.domainEvents().getFirst();
            assertThat(event).isInstanceOf(TradeLoanFacilityContractIssuedEvent.class);

            TradeLoanFacilityContractIssuedEvent contractEvent = (TradeLoanFacilityContractIssuedEvent) event;
            assertThat(contractEvent.aggregateId()).isEqualTo(FACILITY_ID);
            assertThat(contractEvent.payload().sanctionedLoanId()).isEqualTo(sanctionedLoan.getId());
            assertThat(contractEvent.payload().transactionNumbers()).containsExactlyElementsOf(transactionNumbers);
            assertThat(contractEvent.eventType()).isEqualTo("TRADE_LOAN_FACILITY_CONTRACT_ISSUED");
        }

        @DisplayName("should preserve transaction numbers immutability in event payload")
        @Test
        void shouldPreserveTransactionNumbersImmutabilityInEventPayload(
                @Mock TradeLoanApplication loanApplication, @Mock TradeSanctionedLoan sanctionedLoan) {
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> originalTransactionNumbers = ImmutableList.of(
                    TransactionNumber.of("TXN-TRADE-001").orElseThrow(),
                    TransactionNumber.of("TXN-TRADE-002").orElseThrow());
            when(sanctionedLoan.getId()).thenReturn(TradeSanctionedLoanId.generate());

            TradeLoanFacility facility = createFacility(FacilityStatus.APPROVED, loanApplication, sanctionedLoan);
            facility.issueContract(originalTransactionNumbers, clock);

            TradeLoanFacilityContractIssuedEvent event = (TradeLoanFacilityContractIssuedEvent)
                    facility.domainEvents().getFirst();
            List<TransactionNumber> eventTransactionNumbers = event.payload().transactionNumbers();

            assertThat(eventTransactionNumbers)
                    .isNotSameAs(originalTransactionNumbers)
                    .containsExactlyElementsOf(originalTransactionNumbers);

            assertThatCode(eventTransactionNumbers::clear).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @DisplayName("Trade-Specific Contract Issuance Scenarios")
    @Nested
    final class TradeSpecificContractIssuanceScenariosTest {

        @DisplayName("should successfully issue contract for Murabaha loan type")
        @Test
        void shouldSuccessfullyIssueContractForMurabahaLoanType(
                @Mock TradeLoanApplication loanApplication, @Mock TradeSanctionedLoan sanctionedLoan) {
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> transactionNumbers =
                    ImmutableList.of(TransactionNumber.of("MURABAHA-TXN-001").orElseThrow());
            when(sanctionedLoan.getId()).thenReturn(TradeSanctionedLoanId.generate());

            TradeLoanFacility facility = createFacility(FacilityStatus.APPROVED, loanApplication, sanctionedLoan);

            Result<Void> result = facility.issueContract(transactionNumbers, clock);

            assertThat(result.isSuccess()).isTrue();
            assertThat(facility.getLoanFacilityType()).isEqualTo("TRADE");
            assertThat(facility.getCurrentState()).isEqualTo(FacilityStatus.PENDING_DISBURSEMENT);
            assertThat(facility.getTransactionNumbers()).containsExactlyElementsOf(transactionNumbers);
        }

        @DisplayName("should maintain loan type and arrangement references after contract issuance")
        @Test
        void shouldMaintainLoanTypeAndArrangementReferencesAfterContractIssuance(
                @Mock TradeLoanApplication loanApplication, @Mock TradeSanctionedLoan sanctionedLoan) {
            given(sanctionedLoan.getId()).willReturn(TradeSanctionedLoanId.generate());
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> transactionNumbers =
                    ImmutableList.of(TransactionNumber.of("TXN-TRADE-001").orElseThrow());

            TradeLoanFacility facility = createFacility(FacilityStatus.APPROVED, loanApplication, sanctionedLoan);

            facility.issueContract(transactionNumbers, clock);

            assertThat(facility.loanTypeId()).isEqualTo(LOAN_TYPE_ID);
            assertThat(facility.getLoanArrangementId()).isEqualTo(LOAN_ARRANGEMENT_ID);
            assertThat(facility.getLoanFacilityType()).isEqualTo("TRADE");
        }
    }

    @DisplayName("Trade Loan Integration with Base Loan Functionality")
    @Nested
    final class TradeLoanIntegrationTest {

        @DisplayName("should inherit all base loan facility validations for contract issuance")
        @Test
        void shouldInheritBaseLoanFacilityValidationsForContractIssuance(@Mock TradeLoanApplication loanApplication) {
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> transactionNumbers =
                    ImmutableList.of(TransactionNumber.of("TXN-TRADE-001").orElseThrow());

            TradeLoanFacility facility = createFacility(FacilityStatus.APPROVED, loanApplication, null);

            Result<Void> result = facility.issueContract(transactionNumbers, clock);

            assertFailureHasErrors(result);
            assertThat(facility.getCurrentState()).isEqualTo(FacilityStatus.APPROVED);
            assertThat(facility.domainEvents()).isEmpty();
        }
    }
}
