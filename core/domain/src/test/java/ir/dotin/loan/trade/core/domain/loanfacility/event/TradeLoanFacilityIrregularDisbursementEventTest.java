package ir.dotin.loan.trade.core.domain.loanfacility.event;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ir.dotin.platform.domain.common.vo.CurrencyType;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

final class TradeLoanFacilityIrregularDisbursementEventTest {

    private static final Instant FIXED_TIMESTAMP = Instant.parse("2025-01-01T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_TIMESTAMP, UTC);

    private LoanFacilityId aggregateId;
    private SanctionedLoanId sanctionedLoanId;
    private Money amountToDisburse;

    @BeforeEach
    void setUp() {
        this.aggregateId = new LoanFacilityId(randomUUID());
        this.sanctionedLoanId = new SanctionedLoanId(randomUUID());
        this.amountToDisburse = new Money(new BigDecimal("10000.00"), CurrencyType.IRR);
    }

    @Nested
    @DisplayName("Constructor and Field Access Tests")
    final class ConstructorTests {

        @Test
        @DisplayName("should create event successfully with valid arguments")
        void shouldCreateEventSuccessfullyWithValidArguments() {
            UUID eventId = randomUUID();
            TradeLoanFacilityIrregularDisbursementEvent.Payload payload =
                    new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId);
            TradeLoanFacilityIrregularDisbursementEvent event = new TradeLoanFacilityIrregularDisbursementEvent(
                    eventId, aggregateId, payload, amountToDisburse, FIXED_TIMESTAMP);

            assertThat(event).isNotNull();
            assertThat(event.eventId()).isEqualTo(eventId);
            assertThat(event.aggregateId()).isEqualTo(aggregateId);
            assertThat(event.payload()).isEqualTo(payload);
            assertThat(event.amountToDisburse()).isEqualTo(amountToDisburse);
            assertThat(event.createdAt()).isEqualTo(FIXED_TIMESTAMP);
        }

        @Test
        @DisplayName("should throw NullPointerException if eventId is null")
        @SuppressWarnings("NullAway")
        void shouldThrowExceptionIfEventIdIsNull() {
            assertThatThrownBy(() -> new TradeLoanFacilityIrregularDisbursementEvent(
                            null,
                            aggregateId,
                            new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId),
                            amountToDisburse,
                            FIXED_TIMESTAMP))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException if aggregateId is null")
        @SuppressWarnings("NullAway")
        void shouldThrowExceptionIfAggregateIdIsNull() {
            assertThatThrownBy(() -> new TradeLoanFacilityIrregularDisbursementEvent(
                            randomUUID(),
                            null,
                            new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId),
                            amountToDisburse,
                            FIXED_TIMESTAMP))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException if payload is null")
        @SuppressWarnings("NullAway")
        void shouldThrowExceptionIfPayloadIsNull() {
            assertThatThrownBy(() -> new TradeLoanFacilityIrregularDisbursementEvent(
                            randomUUID(), aggregateId, null, amountToDisburse, FIXED_TIMESTAMP))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException if amountToDisburse is null")
        @SuppressWarnings("NullAway")
        void shouldThrowExceptionIfAmountToDisburseIsNull() {
            assertThatThrownBy(() -> new TradeLoanFacilityIrregularDisbursementEvent(
                            randomUUID(),
                            aggregateId,
                            new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId),
                            null,
                            FIXED_TIMESTAMP))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException if createdAt is null")
        @SuppressWarnings("NullAway")
        void shouldThrowExceptionIfCreatedAtIsNull() {
            assertThatThrownBy(() -> new TradeLoanFacilityIrregularDisbursementEvent(
                            randomUUID(),
                            aggregateId,
                            new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId),
                            amountToDisburse,
                            null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Payload should throw NullPointerException if sanctionedLoanId is null")
        @SuppressWarnings("NullAway")
        void payloadShouldThrowExceptionIfSanctionedLoanIdIsNull() {
            assertThatThrownBy(() -> new TradeLoanFacilityIrregularDisbursementEvent.Payload(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Static Factory Method 'of()' Tests")
    final class OfMethodTests {

        @Test
        @DisplayName("should create event with 'of()' factory method")
        void shouldCreateEventWithOfMethod() {
            TradeLoanFacilityIrregularDisbursementEvent event = TradeLoanFacilityIrregularDisbursementEvent.of(
                    aggregateId, sanctionedLoanId, amountToDisburse, FIXED_CLOCK);

            assertThat(event).isNotNull();
            assertThat(event.eventId()).isNotNull();
            assertThat(event.aggregateId()).isEqualTo(aggregateId);
            assertThat(event.amountToDisburse()).isEqualTo(amountToDisburse);
            assertThat(event.createdAt()).isEqualTo(FIXED_CLOCK.instant());
            assertThat(event.payload()).isNotNull();
            assertThat(event.payload().sanctionedLoanId()).isEqualTo(sanctionedLoanId);
        }

        @Test
        @DisplayName("should generate unique event ID for each event")
        void shouldGenerateUniqueEventIdForEachEvent() {
            TradeLoanFacilityIrregularDisbursementEvent event1 = TradeLoanFacilityIrregularDisbursementEvent.of(
                    aggregateId, sanctionedLoanId, amountToDisburse, FIXED_CLOCK);
            TradeLoanFacilityIrregularDisbursementEvent event2 = TradeLoanFacilityIrregularDisbursementEvent.of(
                    aggregateId, sanctionedLoanId, amountToDisburse, FIXED_CLOCK);

            assertThat(event1.eventId()).isNotEqualTo(event2.eventId());
        }

        @Test
        @DisplayName("should throw NullPointerException if id for 'of()' is null")
        @SuppressWarnings("NullAway")
        void shouldThrowExceptionIfIdIsNull() {
            assertThatThrownBy(() -> TradeLoanFacilityIrregularDisbursementEvent.of(
                            null, sanctionedLoanId, amountToDisburse, FIXED_CLOCK))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException if sanId for 'of()' is null")
        @SuppressWarnings("NullAway")
        void shouldThrowExceptionIfSanIdIsNull() {
            assertThatThrownBy(() -> TradeLoanFacilityIrregularDisbursementEvent.of(
                            aggregateId, null, amountToDisburse, FIXED_CLOCK))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException if amountToDisburse for 'of()' is null")
        @SuppressWarnings("NullAway")
        void shouldThrowExceptionIfAmountToDisburseIsNull() {
            assertThatThrownBy(() -> TradeLoanFacilityIrregularDisbursementEvent.of(
                            aggregateId, sanctionedLoanId, null, FIXED_CLOCK))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Behavioral Tests")
    final class BehavioralTests {

        @Test
        @DisplayName("should return correct event type string")
        void shouldReturnCorrectEventType() {
            TradeLoanFacilityIrregularDisbursementEvent event = TradeLoanFacilityIrregularDisbursementEvent.of(
                    aggregateId, sanctionedLoanId, amountToDisburse, FIXED_CLOCK);

            String expectedEventType = TradeLoanFacilityEvent.EVENT_TYPE_PREFIX + "IRREGULAR_DISBURSEMENT";
            assertThat(event.eventType()).isEqualTo(expectedEventType);
        }
    }
}
