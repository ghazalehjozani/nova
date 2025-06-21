package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("TradeLoanFacilityContractIssuedEvent Tests")
@SuppressWarnings({"NullAway", "TimeZoneUsage"})
final class TradeLoanFacilityContractIssuedEventTest {

    private static final TradeLoanFacilityId FACILITY_ID = TradeLoanFacilityId.generate();
    private static final TradeSanctionedLoanId SANCTIONED_LOAN_ID = TradeSanctionedLoanId.generate();

    @DisplayName("Event Creation and Structure")
    @Nested
    final class EventCreationAndStructureTest {

        @DisplayName("should create event with all required fields using factory method")
        @Test
        void shouldCreateEventWithAllRequiredFieldsUsingFactoryMethod() {
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> transactionNumbers = List.of(
                    TransactionNumber.of("TXN-TRADE-001").orElseThrow(),
                    TransactionNumber.of("TXN-TRADE-002").orElseThrow());

            TradeLoanFacilityContractIssuedEvent event =
                    TradeLoanFacilityContractIssuedEvent.of(FACILITY_ID, SANCTIONED_LOAN_ID, transactionNumbers, clock);

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
            List<TransactionNumber> transactionNumbers =
                    List.of(TransactionNumber.of("TXN-001").orElseThrow());

            TradeLoanFacilityContractIssuedEvent event =
                    TradeLoanFacilityContractIssuedEvent.of(FACILITY_ID, SANCTIONED_LOAN_ID, transactionNumbers, clock);

            assertThat(event.eventType()).isEqualTo("TRADE_LOAN_FACILITY_CONTRACT_ISSUED");
        }

        @DisplayName("should generate unique event ID for each event")
        @Test
        void shouldGenerateUniqueEventIdForEachEvent() {
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> transactionNumbers =
                    List.of(TransactionNumber.of("TXN-001").orElseThrow());

            TradeLoanFacilityContractIssuedEvent event1 =
                    TradeLoanFacilityContractIssuedEvent.of(FACILITY_ID, SANCTIONED_LOAN_ID, transactionNumbers, clock);
            TradeLoanFacilityContractIssuedEvent event2 =
                    TradeLoanFacilityContractIssuedEvent.of(FACILITY_ID, SANCTIONED_LOAN_ID, transactionNumbers, clock);

            assertThat(event1.eventId()).isNotEqualTo(event2.eventId());
        }
    }

    @DisplayName("Payload Structure and Validation")
    @Nested
    final class PayloadStructureAndValidationTest {

        @DisplayName("should create payload with sanctioned loan ID and transaction numbers")
        @Test
        void shouldCreatePayloadWithSanctionedLoanIdAndTransactionNumbers() {
            List<TransactionNumber> transactionNumbers = List.of(
                    TransactionNumber.of("TXN-TRADE-001").orElseThrow(),
                    TransactionNumber.of("TXN-TRADE-002").orElseThrow(),
                    TransactionNumber.of("TXN-TRADE-003").orElseThrow());

            TradeLoanFacilityContractIssuedEvent.Payload payload =
                    new TradeLoanFacilityContractIssuedEvent.Payload(SANCTIONED_LOAN_ID, transactionNumbers);

            assertThat(payload.sanctionedLoanId()).isEqualTo(SANCTIONED_LOAN_ID);
            assertThat(payload.transactionNumbers()).containsExactlyElementsOf(transactionNumbers);
            assertThat(payload.transactionNumbers()).hasSize(3);
        }

        @DisplayName("should preserve transaction numbers immutability in payload")
        @Test
        void shouldPreserveTransactionNumbersImmutabilityInPayload() {
            List<TransactionNumber> originalTransactionNumbers = List.of(
                    TransactionNumber.of("TXN-TRADE-001").orElseThrow(),
                    TransactionNumber.of("TXN-TRADE-002").orElseThrow());

            TradeLoanFacilityContractIssuedEvent.Payload payload =
                    new TradeLoanFacilityContractIssuedEvent.Payload(SANCTIONED_LOAN_ID, originalTransactionNumbers);

            List<TransactionNumber> retrievedTransactionNumbers = payload.transactionNumbers();
            assertThat(retrievedTransactionNumbers).containsExactlyElementsOf(originalTransactionNumbers);

            assertThatCode(retrievedTransactionNumbers::clear).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("should handle empty transaction numbers list")
        @Test
        void shouldHandleEmptyTransactionNumbersList() {
            List<TransactionNumber> emptyTransactionNumbers = List.of();

            TradeLoanFacilityContractIssuedEvent.Payload payload =
                    new TradeLoanFacilityContractIssuedEvent.Payload(SANCTIONED_LOAN_ID, emptyTransactionNumbers);

            assertThat(payload.transactionNumbers()).isEmpty();
            assertThat(payload.transactionNumbers()).isNotNull();
        }

        @DisplayName("should reject null sanctioned loan ID in payload")
        @Test
        void shouldRejectNullSanctionedLoanIdInPayload() {
            List<TransactionNumber> transactionNumbers =
                    List.of(TransactionNumber.of("TXN-001").orElseThrow());

            assertThatCode(() -> new TradeLoanFacilityContractIssuedEvent.Payload(null, transactionNumbers))
                    .isInstanceOf(NullPointerException.class);
        }

        @DisplayName("should reject null transaction numbers in payload")
        @Test
        void shouldRejectNullTransactionNumbersInPayload() {
            assertThatCode(() -> new TradeLoanFacilityContractIssuedEvent.Payload(SANCTIONED_LOAN_ID, null))
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
            List<TransactionNumber> originalTransactionNumbers = List.of(
                    TransactionNumber.of("TXN-TRADE-001").orElseThrow(),
                    TransactionNumber.of("TXN-TRADE-002").orElseThrow());

            TradeLoanFacilityContractIssuedEvent event = TradeLoanFacilityContractIssuedEvent.of(
                    FACILITY_ID, SANCTIONED_LOAN_ID, originalTransactionNumbers, clock);

            List<TransactionNumber> eventTransactionNumbers = event.payload().transactionNumbers();
            assertThat(eventTransactionNumbers)
                    .isSameAs(originalTransactionNumbers)
                    .containsExactlyElementsOf(originalTransactionNumbers);
        }

        @DisplayName("should handle single transaction number")
        @Test
        void shouldHandleSingleTransactionNumber() {
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> singleTransactionNumber =
                    List.of(TransactionNumber.of("TXN-SINGLE-001").orElseThrow());

            TradeLoanFacilityContractIssuedEvent event = TradeLoanFacilityContractIssuedEvent.of(
                    FACILITY_ID, SANCTIONED_LOAN_ID, singleTransactionNumber, clock);

            assertThat(event.payload().transactionNumbers()).hasSize(1);
            assertThat(event.payload().transactionNumbers().getFirst().value()).isEqualTo("TXN-SINGLE-001");
        }

        @DisplayName("should handle multiple transaction numbers")
        @Test
        void shouldHandleMultipleTransactionNumbers() {
            Clock clock = Clock.systemUTC();
            List<TransactionNumber> multipleTransactionNumbers = List.of(
                    TransactionNumber.of("TXN-MULTI-001").orElseThrow(),
                    TransactionNumber.of("TXN-MULTI-002").orElseThrow(),
                    TransactionNumber.of("TXN-MULTI-003").orElseThrow(),
                    TransactionNumber.of("TXN-MULTI-004").orElseThrow());

            TradeLoanFacilityContractIssuedEvent event = TradeLoanFacilityContractIssuedEvent.of(
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
            List<TransactionNumber> transactionNumbers =
                    List.of(TransactionNumber.of("TXN-001").orElseThrow());
            TradeLoanFacilityContractIssuedEvent.Payload payload =
                    new TradeLoanFacilityContractIssuedEvent.Payload(SANCTIONED_LOAN_ID, transactionNumbers);

            assertThatCode(() -> new TradeLoanFacilityContractIssuedEvent(null, FACILITY_ID, payload, now))
                    .isInstanceOf(NullPointerException.class);
        }

        @DisplayName("should reject null aggregate ID")
        @Test
        void shouldRejectNullAggregateId() {
            UUID eventId = randomUUID();
            Instant now = Instant.now();
            List<TransactionNumber> transactionNumbers =
                    List.of(TransactionNumber.of("TXN-001").orElseThrow());
            TradeLoanFacilityContractIssuedEvent.Payload payload =
                    new TradeLoanFacilityContractIssuedEvent.Payload(SANCTIONED_LOAN_ID, transactionNumbers);

            assertThatCode(() -> new TradeLoanFacilityContractIssuedEvent(eventId, null, payload, now))
                    .isInstanceOf(NullPointerException.class);
        }

        @DisplayName("should reject null payload")
        @Test
        void shouldRejectNullPayload() {
            UUID eventId = randomUUID();
            Instant now = Instant.now();

            assertThatCode(() -> new TradeLoanFacilityContractIssuedEvent(eventId, FACILITY_ID, null, now))
                    .isInstanceOf(NullPointerException.class);
        }

        @DisplayName("should reject null created at timestamp")
        @Test
        void shouldRejectNullCreatedAtTimestamp() {
            UUID eventId = randomUUID();
            List<TransactionNumber> transactionNumbers =
                    List.of(TransactionNumber.of("TXN-001").orElseThrow());
            TradeLoanFacilityContractIssuedEvent.Payload payload =
                    new TradeLoanFacilityContractIssuedEvent.Payload(SANCTIONED_LOAN_ID, transactionNumbers);

            assertThatCode(() -> new TradeLoanFacilityContractIssuedEvent(eventId, FACILITY_ID, payload, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
