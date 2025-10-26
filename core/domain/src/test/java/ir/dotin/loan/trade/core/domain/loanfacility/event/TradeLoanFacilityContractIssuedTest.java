package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("TradeLoanFacilityContractIssuedEvent Tests")
@SuppressWarnings({"NullAway", "TimeZoneUsage"})
final class TradeLoanFacilityContractIssuedTest {

    private static final LoanFacilityId FACILITY_ID = LoanFacilityId.of(randomUUID());
    private static final SanctionedLoanId SANCTIONED_LOAN_ID = SanctionedLoanId.of(randomUUID());

    @DisplayName("Event Creation and Structure")
    @Nested
    final class EventCreationAndStructureTest {

        @DisplayName("should create event with all required fields using factory method")
        @Test
        void shouldCreateEventWithAllRequiredFieldsUsingFactoryMethod() {
            Clock clock = Clock.systemUTC();
            List<String> transactionNumbers = ImmutableList.of("TXN-TRADE-001", "TXN-TRADE-002");

            TradeLoanFacilityContractIssued event =
                    TradeLoanFacilityContractIssued.of(FACILITY_ID, SANCTIONED_LOAN_ID, transactionNumbers, clock);

            assertThat(event.eventId()).isNotNull();
            assertThat(event.aggregateId()).isEqualTo(FACILITY_ID);
            assertThat(event.payload()).isNotNull();
            assertThat(event.payload().sanctionedLoanId()).isEqualTo(SANCTIONED_LOAN_ID);
            assertThat(event.payload().transactionNumbers()).containsExactlyElementsOf(transactionNumbers);
            assertThat(event.createdAt()).isNotNull();
            assertThat(event.eventType()).isEqualTo("TRADE_LOAN_FACILITY_CONTRACT_ISSUED");
        }

        @DisplayName("should create event with correct event type")
        @Test
        void shouldCreateEventWithCorrectEventType() {
            Clock clock = Clock.systemUTC();
            List<String> transactionNumbers = ImmutableList.of("TXN-001");

            TradeLoanFacilityContractIssued event =
                    TradeLoanFacilityContractIssued.of(FACILITY_ID, SANCTIONED_LOAN_ID, transactionNumbers, clock);

            assertThat(event.eventType()).isEqualTo("TRADE_LOAN_FACILITY_CONTRACT_ISSUED");
        }

        @DisplayName("should generate unique event ID for each event")
        @Test
        void shouldGenerateUniqueEventIdForEachEvent() {
            Clock clock = Clock.systemUTC();
            List<String> transactionNumbers = ImmutableList.of("TXN-001");

            TradeLoanFacilityContractIssued event1 =
                    TradeLoanFacilityContractIssued.of(FACILITY_ID, SANCTIONED_LOAN_ID, transactionNumbers, clock);
            TradeLoanFacilityContractIssued event2 =
                    TradeLoanFacilityContractIssued.of(FACILITY_ID, SANCTIONED_LOAN_ID, transactionNumbers, clock);

            assertThat(event1.eventId()).isNotEqualTo(event2.eventId());
        }
    }

    @DisplayName("Payload Structure and Validation")
    @Nested
    final class PayloadStructureAndValidationTest {

        @DisplayName("should create payload with sanctioned loan ID and transaction numbers")
        @Test
        void shouldCreatePayloadWithSanctionedLoanIdAndTransactionNumbers() {
            List<String> transactionNumbers = ImmutableList.of("TXN-TRADE-001", "TXN-TRADE-002", "TXN-TRADE-003");

            TradeLoanFacilityContractIssued.Payload payload =
                    new TradeLoanFacilityContractIssued.Payload(SANCTIONED_LOAN_ID, transactionNumbers);

            assertThat(payload.sanctionedLoanId()).isEqualTo(SANCTIONED_LOAN_ID);
            assertThat(payload.transactionNumbers()).containsExactlyElementsOf(transactionNumbers);
            assertThat(payload.transactionNumbers()).hasSize(3);
        }

        @DisplayName("should preserve transaction numbers immutability in payload")
        @Test
        void shouldPreserveTransactionNumbersImmutabilityInPayload() {
            List<String> originalTransactionNumbers = ImmutableList.of("TXN-TRADE-001", "TXN-TRADE-002");

            TradeLoanFacilityContractIssued.Payload payload =
                    new TradeLoanFacilityContractIssued.Payload(SANCTIONED_LOAN_ID, originalTransactionNumbers);

            List<String> retrievedTransactionNumbers = payload.transactionNumbers();
            assertThat(retrievedTransactionNumbers).containsExactlyElementsOf(originalTransactionNumbers);

            assertThatCode(retrievedTransactionNumbers::clear).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("should handle empty transaction numbers list")
        @Test
        void shouldHandleEmptyTransactionNumbersList() {
            List<String> emptyTransactionNumbers = ImmutableList.of();

            TradeLoanFacilityContractIssued.Payload payload =
                    new TradeLoanFacilityContractIssued.Payload(SANCTIONED_LOAN_ID, emptyTransactionNumbers);

            assertThat(payload.transactionNumbers()).isEmpty();
            assertThat(payload.transactionNumbers()).isNotNull();
        }

        @DisplayName("should reject null sanctioned loan ID in payload")
        @Test
        void shouldRejectNullSanctionedLoanIdInPayload() {
            List<String> transactionNumbers = ImmutableList.of("TXN-001");

            assertThatCode(() -> new TradeLoanFacilityContractIssued.Payload(null, transactionNumbers))
                    .isInstanceOf(NullPointerException.class);
        }

        @DisplayName("should reject null transaction numbers in payload")
        @Test
        void shouldRejectNullTransactionNumbersInPayload() {
            assertThatCode(() -> new TradeLoanFacilityContractIssued.Payload(SANCTIONED_LOAN_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @DisplayName("Event Factory Method Validation")
    @Nested
    final class EventFactoryMethodValidationTest {

        @DisplayName("should copy transaction numbers list in factory method")
        @Test
        void shouldCopyTransactionNumbersListInFactoryMethod() {
            Clock clock = Clock.systemUTC();
            List<String> originalTransactionNumbers = ImmutableList.of("TXN-TRADE-001", "TXN-TRADE-002");

            TradeLoanFacilityContractIssued event = TradeLoanFacilityContractIssued.of(
                    FACILITY_ID, SANCTIONED_LOAN_ID, originalTransactionNumbers, clock);

            List<String> eventTransactionNumbers = event.payload().transactionNumbers();
            assertThat(eventTransactionNumbers)
                    .isSameAs(originalTransactionNumbers)
                    .containsExactlyElementsOf(originalTransactionNumbers);
        }

        @DisplayName("should handle single transaction number")
        @Test
        void shouldHandleSingleTransactionNumber() {
            Clock clock = Clock.systemUTC();
            List<String> singleTransactionNumber = ImmutableList.of("TXN-SINGLE-001");

            TradeLoanFacilityContractIssued event =
                    TradeLoanFacilityContractIssued.of(FACILITY_ID, SANCTIONED_LOAN_ID, singleTransactionNumber, clock);

            assertThat(event.payload().transactionNumbers()).hasSize(1);
            assertThat(event.payload().transactionNumbers().getFirst()).isEqualTo("TXN-SINGLE-001");
        }

        @DisplayName("should handle multiple transaction numbers")
        @Test
        void shouldHandleMultipleTransactionNumbers() {
            Clock clock = Clock.systemUTC();
            List<String> multipleTransactionNumbers =
                    ImmutableList.of("TXN-MULTI-001", "TXN-MULTI-002", "TXN-MULTI-003", "TXN-MULTI-004");

            TradeLoanFacilityContractIssued event = TradeLoanFacilityContractIssued.of(
                    FACILITY_ID, SANCTIONED_LOAN_ID, multipleTransactionNumbers, clock);

            assertThat(event.payload().transactionNumbers()).hasSize(4);
            assertThat(event.payload().transactionNumbers()).containsExactlyElementsOf(multipleTransactionNumbers);
        }
    }

    @DisplayName("Event Record Validation")
    @Nested
    final class EventRecordValidationTest {

        @DisplayName("should reject null event ID")
        @Test
        void shouldRejectNullEventId() {
            Instant now = Instant.now();
            List<String> transactionNumbers = ImmutableList.of("TXN-001");
            TradeLoanFacilityContractIssued.Payload payload =
                    new TradeLoanFacilityContractIssued.Payload(SANCTIONED_LOAN_ID, transactionNumbers);

            assertThatCode(() -> new TradeLoanFacilityContractIssued(null, FACILITY_ID, payload, now))
                    .isInstanceOf(NullPointerException.class);
        }

        @DisplayName("should reject null aggregate ID")
        @Test
        void shouldRejectNullAggregateId() {
            UUID eventId = randomUUID();
            Instant now = Instant.now();
            List<String> transactionNumbers = ImmutableList.of("TXN-001");
            TradeLoanFacilityContractIssued.Payload payload =
                    new TradeLoanFacilityContractIssued.Payload(SANCTIONED_LOAN_ID, transactionNumbers);

            assertThatCode(() -> new TradeLoanFacilityContractIssued(eventId, null, payload, now))
                    .isInstanceOf(NullPointerException.class);
        }

        @DisplayName("should reject null payload")
        @Test
        void shouldRejectNullPayload() {
            UUID eventId = randomUUID();
            Instant now = Instant.now();

            assertThatCode(() -> new TradeLoanFacilityContractIssued(eventId, FACILITY_ID, null, now))
                    .isInstanceOf(NullPointerException.class);
        }

        @DisplayName("should reject null created at timestamp")
        @Test
        void shouldRejectNullCreatedAtTimestamp() {
            UUID eventId = randomUUID();
            List<String> transactionNumbers = ImmutableList.of("TXN-001");
            TradeLoanFacilityContractIssued.Payload payload =
                    new TradeLoanFacilityContractIssued.Payload(SANCTIONED_LOAN_ID, transactionNumbers);

            assertThatCode(() -> new TradeLoanFacilityContractIssued(eventId, FACILITY_ID, payload, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
