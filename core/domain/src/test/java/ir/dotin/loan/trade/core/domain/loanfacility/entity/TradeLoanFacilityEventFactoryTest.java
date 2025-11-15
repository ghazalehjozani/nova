package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityAdditionalDisbursementCompleted;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityApprovalSubmitted;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityApproved;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCancelled;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityClosedDefaulted;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCollateralAdded;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityContractIssued;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCreated;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityDisbursementFailed;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityLumpSumDisbursed;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityPaidOffClosed;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityPartiallyDisbursed;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityRejected;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

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
    private CollateralSerial mockCollateralSerial;

    @Mock
    private FailureReason mockFailureReason;

    @Mock
    private ApplicationNumber mockApplicationNumber;

    @Mock
    private Money mockMoney;

    @Mock
    private TrackedTransactionNumber mockTrackedTransactionNumber;

    private TradeLoanFacilityEventFactory factory;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        factory = new TradeLoanFacilityEventFactory();
        fixedClock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), UTC);
        lenient().when(mockFacilityId.value()).thenReturn(UUID.randomUUID());
        lenient().when(mockApplicationId.value()).thenReturn(UUID.randomUUID());
        lenient().when(mockSanctionId.value()).thenReturn(UUID.randomUUID());
        lenient().when(mockApplicationNumber.formattedApplicationNumber()).thenReturn("APP-12345");
        lenient().when(mockTrackedTransactionNumber.value()).thenReturn("TRX-67890");

        // Mock Money behavior to avoid NullPointerException
        lenient().when(mockMoney.currency()).thenReturn(CurrencyType.IRR);
        lenient().when(mockMoney.toString()).thenReturn("1000000");
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

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityApprovalSubmitted.class);
        }

        @Test
        @DisplayName("should create approved event")
        void shouldCreateApprovedEvent() {
            var event = factory.createApprovedEvent(
                    mockFacilityId, mockSanctionId, "SanctionSerial", SanctionType.GENERAL, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityApproved.class);
        }

        @Test
        @DisplayName("should create rejected event")
        void shouldCreateRejectedEvent() {
            var event = factory.createRejectedEvent(mockFacilityId, mockApplicationId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityRejected.class);
        }

        @Test
        @DisplayName("should create contract issued event")
        void shouldCreateContractIssuedEvent() {
            var event = factory.createContractIssuedEvent(mockFacilityId, mockSanctionId, "trx1", fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityContractIssued.class);
        }

        @Test
        @DisplayName("should create disbursement failed event")
        void shouldCreateDisbursementFailedEvent() {
            var event = factory.createDisbursementFailedEvent(
                    mockFacilityId, mockSanctionId, mockFailureReason, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityDisbursementFailed.class);
        }

        @Test
        @DisplayName("should create activated event")
        void shouldCreateActivatedEvent() {
            var event = factory.createLumpSumDisbursedEvent(
                    mockFacilityId,
                    mockSanctionId,
                    InstallmentPaymentType.ONE_TIME,
                    mockApplicationNumber,
                    List.of(mockTrackedTransactionNumber),
                    new InstallmentScheduleId(UUID.randomUUID()),
                    fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityLumpSumDisbursed.class);
        }

        @Test
        @DisplayName("should create closed paid off event")
        void shouldCreateClosedPaidOffEvent() {
            var event = factory.createClosedPaidOffEvent(mockFacilityId, mockSanctionId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityPaidOffClosed.class);
        }

        @Test
        @DisplayName("should create closed defaulted event")
        void shouldCreateClosedDefaultedEvent() {
            var event = factory.createClosedDefaultedEvent(mockFacilityId, mockSanctionId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityClosedDefaulted.class);
        }

        @Test
        @DisplayName("should create cancelled event")
        void shouldCreateCancelledEvent() {
            var event = factory.createCancelledEvent(mockFacilityId, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityCancelled.class);
        }

        @Test
        @DisplayName("should create collateral added event")
        void shouldCreateCollateralAddedEvent() {
            given(mockCollateralSerial.value()).willReturn("serial");
            var event = factory.createCollateralAddedEvent(
                    mockFacilityId, mockSanctionId, mockCollateralSerial, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityCollateralAdded.class);
        }

        @Test
        @DisplayName("should create created event")
        void shouldCreateCreatedEvent() {
            given(mockApplicationNumber.formattedApplicationNumber()).willReturn("appNumber");
            var event = factory.createCreatedEvent(mockFacilityId, mockApplicationNumber, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityCreated.class);
        }

        @Test
        @DisplayName("should create partially disbursed event")
        void shouldCreatePartiallyDisbursedEvent() {
            var event = factory.createPartiallyDisbursedEvent(mockFacilityId, mockSanctionId, mockMoney, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityPartiallyDisbursed.class);
        }

        @Test
        @DisplayName("should create additional disbursement completed event")
        void shouldCreateAdditionalDisbursementCompletedEvent() {
            var event = factory.createAdditionalDisbursementCompletedEvent(
                    mockFacilityId, mockSanctionId, mockMoney, fixedClock);

            assertThat(event).isNotNull().isInstanceOf(TradeLoanFacilityAdditionalDisbursementCompleted.class);
        }
    }
}
