package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.domain.loanfacility.event.*;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TradeLoanFacilityEventFactory Test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
final class TradeLoanFacilityEventFactoryTest {

    @Mock
    private LoanFacilityId mockFacilityId;

    @Mock
    private LoanApplicationId mockApplicationId;

    @Mock
    private SanctionedLoanId mockSanctionId;

    @Mock
    private SanctionSerial mockSanctionSerial;

    @Mock
    private CollateralSerial mockCollateralSerial;

    @Mock
    private FailureReason mockFailureReason;

    @Mock
    private Party mockCustomer;

    @Mock
    private TransactionNumber mockTransactionNumber;

    @Mock
    private Money mockMoney;

    private TradeLoanFacilityEventFactory factory;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        factory = new TradeLoanFacilityEventFactory();
        fixedClock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), UTC);
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {

        @Test
        @DisplayName("should create factory instance")
        void shouldCreateFactoryInstance() {
            assertThat(factory).isNotNull();
        }

        @Test
        @DisplayName("should be a final class")
        void shouldBeAFinalClass() {
            assertThat(TradeLoanFacilityEventFactory.class).isFinal();
        }
    }

    @Nested
    @DisplayName("Event Creation Tests")
    final class EventCreationTests {

        @Test
        @DisplayName("should create pending approval event")
        void shouldCreatePendingApprovalEvent() {
            var event = factory.createPendingApprovalEvent(mockFacilityId, mockApplicationId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityPendingApprovalEvent.class);
        }

        @Test
        @DisplayName("should create approved event")
        void shouldCreateApprovedEvent() {
            var event = factory.createApprovedEvent(mockFacilityId, mockSanctionId, mockSanctionSerial, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityApprovedEvent.class);
        }

        @Test
        @DisplayName("should create rejected event")
        void shouldCreateRejectedEvent() {
            var event = factory.createRejectedEvent(mockFacilityId, mockApplicationId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityRejectedEvent.class);
        }

        @Test
        @DisplayName("should create contract issued event")
        void shouldCreateContractIssuedEvent() {
            var event = factory.createContractIssuedEvent(mockFacilityId, mockSanctionId, List.of("trx1"), fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityContractIssuedEvent.class);
        }

        @Test
        @DisplayName("should create pending disbursement event")
        void shouldCreatePendingDisbursementEvent() {
            var event = factory.createPendingDisbursementEvent(mockFacilityId, mockSanctionId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityPendingDisbursementEvent.class);
        }

        @Test
        @DisplayName("should create disbursement failed event")
        void shouldCreateDisbursementFailedEvent() {
            var event = factory.createDisbursementFailedEvent(
                    mockFacilityId, mockSanctionId, mockFailureReason, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityDisbursementFailedEvent.class);
        }

        @Test
        @DisplayName("should create activated event")
        void shouldCreateActivatedEvent() {
            var event = factory.createActivatedEvent(mockFacilityId, mockSanctionId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityActivatedEvent.class);
        }

        @Test
        @DisplayName("should create closed paid off event")
        void shouldCreateClosedPaidOffEvent() {
            var event = factory.createClosedPaidOffEvent(mockFacilityId, mockSanctionId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityClosedPaidOffEvent.class);
        }

        @Test
        @DisplayName("should create closed defaulted event")
        void shouldCreateClosedDefaultedEvent() {
            var event = factory.createClosedDefaultedEvent(mockFacilityId, mockSanctionId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityClosedDefaultedEvent.class);
        }

        @Test
        @DisplayName("should create cancelled event")
        void shouldCreateCancelledEvent() {
            var event = factory.createCancelledEvent(mockFacilityId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityCancelledEvent.class);
        }

        @Test
        @DisplayName("should create collateral added event")
        void shouldCreateCollateralAddedEvent() {
            var event = factory.createCollateralAddedEvent(
                    mockFacilityId, mockSanctionId, mockCollateralSerial, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityCollateralAddedEvent.class);
        }

        @Test
        @DisplayName("should create created event")
        void shouldCreateCreatedEvent() {
            var event = factory.createCreatedEvent(mockFacilityId, mockApplicationId, mockCustomer, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityCreatedEvent.class);
        }

        @Test
        @DisplayName("should create partially disbursed event")
        void shouldCreatePartiallyDisbursedEvent() {
            var event = factory.createPartiallyDisbursedEvent(mockFacilityId, mockSanctionId, mockMoney, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityPartiallyDisbursedEvent.class);
        }

        @Test
        @DisplayName("should create additional disbursement completed event")
        void shouldCreateAdditionalDisbursementCompletedEvent() {
            var event = factory.createAdditionalDisbursementCompletedEvent(
                    mockFacilityId, mockSanctionId, mockMoney, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityAdditionalDisbursementCompletedEvent.class);
        }
    }

    @Nested
    @DisplayName("ID Generation Tests")
    final class IdGenerationTests {

        @Test
        @DisplayName("should generate sanctioned loan id")
        void shouldGenerateSanctionedLoanId() {
            var id = factory.generateSanctionedLoanId();

            assertThat(id).isNotNull().isInstanceOf(SanctionedLoanId.class);
        }

        @Test
        @DisplayName("should generate different ids on multiple calls")
        void shouldGenerateDifferentIdsOnMultipleCalls() {
            var id1 = factory.generateSanctionedLoanId();
            var id2 = factory.generateSanctionedLoanId();

            assertThat(id1).isNotEqualTo(id2);
        }
    }
}
