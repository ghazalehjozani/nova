package ir.dotin.loan.trade.core.domain.loanfacility.event;


import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import ir.dotin.platform.domain.common.vo.CurrencyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;


final class TradeLoanFacilityIrregularDisbursementEventTest {

    private static final Instant FIXED_TIMESTAMP = Instant.parse("2025-01-01T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_TIMESTAMP, ZoneOffset.UTC);

    private TradeLoanFacilityId aggregateId;
    private TradeSanctionedLoanId sanctionedLoanId;
    private Money amountToDisburse;

    @BeforeEach
    void setUp() {
        this.aggregateId = new TradeLoanFacilityId(randomUUID());
        this.sanctionedLoanId = new TradeSanctionedLoanId(randomUUID());
        this.amountToDisburse = new Money(new BigDecimal("10000.00"), CurrencyType.IRR);
    }

    @Nested
    @DisplayName("Constructor and Field Access Tests")
    final class ConstructorTests {

        @Test
        @DisplayName("Should create event successfully with valid arguments")
        void shouldCreateEventSuccessfullyWithValidArguments() {
            UUID eventId = randomUUID();
            TradeLoanFacilityIrregularDisbursementEvent.Payload payload =
                    new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId);
            TradeLoanFacilityIrregularDisbursementEvent event =
                    new TradeLoanFacilityIrregularDisbursementEvent(
                            eventId, aggregateId, payload, amountToDisburse, FIXED_TIMESTAMP);

            assertNotNull(event);
            assertEquals(eventId, event.eventId());
            assertEquals(aggregateId, event.aggregateId());
            assertEquals(payload, event.payload());
            assertEquals(amountToDisburse, event.amountToDisburse());
            assertEquals(FIXED_TIMESTAMP, event.createdAt());
        }

        @Test
        @DisplayName("Should throw NullPointerException if eventId is null")
        void shouldThrowExceptionIfEventIdIsNull() {
            assertThrows(NullPointerException.class, () -> new TradeLoanFacilityIrregularDisbursementEvent(
                    null,
                    aggregateId,
                    new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId),
                    amountToDisburse,
                    FIXED_TIMESTAMP));
        }

        @Test
        @DisplayName("Should throw NullPointerException if aggregateId is null")
        void shouldThrowExceptionIfAggregateIdIsNull() {
            assertThrows(NullPointerException.class, () -> new TradeLoanFacilityIrregularDisbursementEvent(
                    randomUUID(),
                    null,
                    new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId),
                    amountToDisburse,
                    FIXED_TIMESTAMP));
        }

        @Test
        @DisplayName("Should throw NullPointerException if payload is null")
        void shouldThrowExceptionIfPayloadIsNull() {
            assertThrows(NullPointerException.class, () -> new TradeLoanFacilityIrregularDisbursementEvent(
                    randomUUID(),
                    aggregateId,
                    null,
                    amountToDisburse,
                    FIXED_TIMESTAMP));
        }

        @Test
        @DisplayName("Should throw NullPointerException if amountToDisburse is null")
        void shouldThrowExceptionIfAmountToDisburseIsNull() {
            assertThrows(NullPointerException.class, () -> new TradeLoanFacilityIrregularDisbursementEvent(
                    randomUUID(),
                    aggregateId,
                    new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId),
                    null,
                    FIXED_TIMESTAMP));
        }

        @Test
        @DisplayName("Should throw NullPointerException if createdAt is null")
        void shouldThrowExceptionIfCreatedAtIsNull() {
            assertThrows(NullPointerException.class, () -> new TradeLoanFacilityIrregularDisbursementEvent(
                    randomUUID(),
                    aggregateId,
                    new TradeLoanFacilityIrregularDisbursementEvent.Payload(sanctionedLoanId),
                    amountToDisburse,
                    null));
        }

        @Test
        @DisplayName("Payload should throw NullPointerException if sanctionedLoanId is null")
        void payloadShouldThrowExceptionIfSanctionedLoanIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new TradeLoanFacilityIrregularDisbursementEvent.Payload(null));
        }
    }

    @Nested
    @DisplayName("Static Factory Method 'of()' Tests")
    final class OfMethodTests {

        @Test
        @DisplayName("Should create event with 'of()' factory method")
        void shouldCreateEventWithOfMethod() {
            TradeLoanFacilityIrregularDisbursementEvent event = TradeLoanFacilityIrregularDisbursementEvent.of(
                    aggregateId, sanctionedLoanId, amountToDisburse, FIXED_CLOCK);

            assertNotNull(event);
            assertNotNull(event.eventId());
            assertEquals(aggregateId, event.aggregateId());
            assertEquals(amountToDisburse, event.amountToDisburse());
            assertEquals(FIXED_CLOCK.instant(), event.createdAt());
            assertNotNull(event.payload());
            assertEquals(sanctionedLoanId, event.payload().sanctionedLoanId());
        }

        @Test
        @DisplayName("Should throw NullPointerException if id for 'of()' is null")
        void shouldThrowExceptionIfIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    TradeLoanFacilityIrregularDisbursementEvent.of(null, sanctionedLoanId, amountToDisburse, FIXED_CLOCK));
        }

        @Test
        @DisplayName("Should throw NullPointerException if sanId for 'of()' is null")
        void shouldThrowExceptionIfSanIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    TradeLoanFacilityIrregularDisbursementEvent.of(aggregateId, null, amountToDisburse, FIXED_CLOCK));
        }

        @Test
        @DisplayName("Should throw NullPointerException if amountToDisburse for 'of()' is null")
        void shouldThrowExceptionIfAmountToDisburseIsNull() {
            assertThrows(NullPointerException.class, () ->
                    TradeLoanFacilityIrregularDisbursementEvent.of(aggregateId, sanctionedLoanId, null, FIXED_CLOCK));
        }
    }
}
