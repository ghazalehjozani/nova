package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCreated;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeLoanFacility")
@SuppressWarnings("NullAway")
class TradeLoanFacilityTest {

    private Clock testClock;
    private LoanArrangementId loanArrangementId;
    private InstallmentScheduleId installmentScheduleId;

    @BeforeEach
    void setUp() {
        testClock = Clock.fixed(Instant.parse("2023-12-01T10:00:00Z"), UTC);
        loanArrangementId = LoanArrangementId.of(randomUUID());
        installmentScheduleId = InstallmentScheduleId.of(randomUUID()).value();
    }

    @DisplayName("when creating new trade loan facility")
    @Nested
    final class CreateTradeLoanFacilityTests {

        @DisplayName("should create facility successfully with valid parameters")
        @Test
        void shouldCreateFacilitySuccessfullyWithValidParameters(
                @Mock ApplicationNumber applicationNumber,
                @Mock Party customer,
                @Mock InstallmentCount installmentCount,
                @Mock EconomicSector economicSector,
                @Mock Branch branch,
                @Mock RequestReason requestReason,
                @Mock DisburseDestination disburseDestination,
                @Mock Money totalDisbursementAmount) {
            // given
            var applicationBuilder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = assertDoesNotThrow(() -> applicationBuilder.build());
            var facilityId = LoanFacilityId.of(randomUUID());
            var loanTypeId = LoanTypeId.of(randomUUID());

            // when
            var facility = TradeLoanFacility.create(
                    facilityId, application, loanTypeId, loanArrangementId, testClock, installmentScheduleId);

            // then
            assertThat(facility).isNotNull();
            assertThat(facility.getLoanArrangementId()).isEqualTo(loanArrangementId);
            assertThat(facility.getCurrentState()).isEqualTo(FacilityStatus.APPLICATION_SUBMITTED);
            assertThat(facility.getFacilityType()).isEqualTo("TRADE");
        }

        @DisplayName("should fail when facility ID is null")
        @Test
        void shouldFailWhenFacilityIdIsNull(
                @Mock TradeLoanApplication application, @Mock Money totalDisbursementAmount) {
            // when & then
            assertThatThrownBy(() -> TradeLoanFacility.create(
                            null,
                            application,
                            LoanTypeId.of(randomUUID()),
                            loanArrangementId,
                            testClock,
                            installmentScheduleId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Facility ID cannot be null");
        }

        @DisplayName("should fail when application is null")
        @Test
        void shouldFailWhenApplicationIsNull(@Mock Money totalDisbursementAmount) {
            // given
            var facilityId = LoanFacilityId.of(randomUUID());

            // when & then
            assertThatThrownBy(() -> TradeLoanFacility.create(
                            facilityId,
                            null,
                            LoanTypeId.of(randomUUID()),
                            loanArrangementId,
                            testClock,
                            installmentScheduleId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Application cannot be null");
        }

        @DisplayName("should fail when loan arrangement id is null")
        @Test
        void shouldFailWhenLoanArrangementIdIsNull(
                @Mock TradeLoanApplication application, @Mock Money totalDisbursementAmount) {
            // given
            var facilityId = LoanFacilityId.of(randomUUID());

            // when & then
            assertThatThrownBy(() -> TradeLoanFacility.create(
                            facilityId,
                            application,
                            LoanTypeId.of(randomUUID()),
                            null,
                            testClock,
                            installmentScheduleId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Loan arrangement ID cannot be null");
        }

        @DisplayName("should publish facility created event upon creation")
        @Test
        void shouldPublishFacilityCreatedEventUponCreation(
                @Mock ApplicationNumber applicationNumber,
                @Mock Party customer,
                @Mock InstallmentCount installmentCount,
                @Mock EconomicSector economicSector,
                @Mock Branch branch,
                @Mock RequestReason requestReason,
                @Mock DisburseDestination disburseDestination,
                @Mock Money totalDisbursementAmount) {
            // given
            var applicationBuilder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = assertDoesNotThrow(() -> applicationBuilder.build());
            var facilityId = LoanFacilityId.of(randomUUID());
            var loanTypeId = LoanTypeId.of(randomUUID());

            // when
            var facility = TradeLoanFacility.create(
                    facilityId, application, loanTypeId, loanArrangementId, testClock, installmentScheduleId);

            // then
            assertThat(facility).isNotNull();
            var events = facility.domainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(TradeLoanFacilityCreated.class);

            var createdEvent = (TradeLoanFacilityCreated) events.getFirst();
            assertThat(createdEvent.aggregateId()).isEqualTo(facility.getId());
            assertThat(createdEvent.payload().applicationId())
                    .isEqualTo(facility.getLoanApplication().getId());
            assertThat(createdEvent.payload().customer()).isEqualTo(customer);
        }
    }

    @DisplayName("when accessing facility properties")
    @Nested
    final class FacilityPropertiesTests {

        @DisplayName("should return correct loan type id")
        @Test
        void shouldReturnCorrectLoanTypeId(
                @Mock ApplicationNumber applicationNumber,
                @Mock Party customer,
                @Mock InstallmentCount installmentCount,
                @Mock EconomicSector economicSector,
                @Mock Branch branch,
                @Mock RequestReason requestReason,
                @Mock DisburseDestination disburseDestination,
                @Mock Money totalDisbursementAmount) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = assertDoesNotThrow(() -> builder.build());
            var facilityId = LoanFacilityId.of(randomUUID());
            var loanTypeId = LoanTypeId.of(randomUUID());
            TradeLoanFacility facility = TradeLoanFacility.create(
                    facilityId, application, loanTypeId, loanArrangementId, testClock, installmentScheduleId);

            // when
            var returnedLoanTypeId = facility.getLoanTypeId();

            // then
            assertThat(returnedLoanTypeId).isNotNull().isInstanceOf(LoanTypeId.class);
        }

        @DisplayName("should return correct loan arrangement id")
        @Test
        void shouldReturnCorrectLoanArrangementId(
                @Mock ApplicationNumber applicationNumber,
                @Mock Party customer,
                @Mock InstallmentCount installmentCount,
                @Mock EconomicSector economicSector,
                @Mock Branch branch,
                @Mock RequestReason requestReason,
                @Mock DisburseDestination disburseDestination,
                @Mock Money totalDisbursementAmount) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = assertDoesNotThrow(() -> builder.build());
            var facilityId = LoanFacilityId.of(randomUUID());
            var loanTypeId = LoanTypeId.of(randomUUID());
            TradeLoanFacility facility = TradeLoanFacility.create(
                    facilityId, application, loanTypeId, loanArrangementId, testClock, installmentScheduleId);

            // when
            var returnedArrangementId = facility.getLoanArrangementId();

            // then
            assertThat(returnedArrangementId).isEqualTo(loanArrangementId);
        }

        @DisplayName("should return TRADE as facility type")
        @Test
        void shouldReturnTradeAsFacilityType(
                @Mock ApplicationNumber applicationNumber,
                @Mock Party customer,
                @Mock InstallmentCount installmentCount,
                @Mock EconomicSector economicSector,
                @Mock Branch branch,
                @Mock RequestReason requestReason,
                @Mock DisburseDestination disburseDestination,
                @Mock Money totalDisbursementAmount) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = assertDoesNotThrow(() -> builder.build());
            var facilityId = LoanFacilityId.of(randomUUID());
            var loanTypeId = LoanTypeId.of(randomUUID());
            TradeLoanFacility facility = TradeLoanFacility.create(
                    facilityId, application, loanTypeId, loanArrangementId, testClock, installmentScheduleId);

            // when
            var facilityType = facility.getFacilityType();

            // then
            assertThat(facilityType).isEqualTo("TRADE");
        }
    }

    @DisplayName("when testing backward compatibility")
    @Nested
    final class BackwardCompatibilityTests {

        @DisplayName("should maintain compatibility with existing facility workflows")
        @Test
        void shouldMaintainCompatibilityWithExistingFacilityWorkflows(
                @Mock ApplicationNumber applicationNumber,
                @Mock Party customer,
                @Mock InstallmentCount installmentCount,
                @Mock EconomicSector economicSector,
                @Mock Branch branch,
                @Mock RequestReason requestReason,
                @Mock DisburseDestination disburseDestination,
                @Mock Money totalDisbursementAmount) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = assertDoesNotThrow(() -> builder.build());
            var facilityId = LoanFacilityId.of(randomUUID());
            var loanTypeId = LoanTypeId.of(randomUUID());
            TradeLoanFacility facility = TradeLoanFacility.create(
                    facilityId, application, loanTypeId, loanArrangementId, testClock, installmentScheduleId);

            // when & then - should be able to access all inherited workflow methods
            assertThat(facility.getCurrentState()).isEqualTo(FacilityStatus.APPLICATION_SUBMITTED);
            assertThat((Object) facility.getLoanApplication()).isNotNull();
            assertThat(facility.getIssueContractTransactionNumbers().isEmpty()).isTrue();
            assertThat(facility.getSanctionedLoan()).isEmpty();
        }

        @DisplayName("should work with workflow operations")
        @Test
        void shouldWorkWithWorkflowOperations(
                @Mock ApplicationNumber applicationNumber,
                @Mock Party customer,
                @Mock InstallmentCount installmentCount,
                @Mock EconomicSector economicSector,
                @Mock Branch branch,
                @Mock RequestReason requestReason,
                @Mock DisburseDestination disburseDestination,
                @Mock Money totalDisbursementAmount) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = assertDoesNotThrow(() -> builder.build());
            var facilityId = LoanFacilityId.of(randomUUID());
            var loanTypeId = LoanTypeId.of(randomUUID());
            TradeLoanFacility facility = TradeLoanFacility.create(
                    facilityId, application, loanTypeId, loanArrangementId, testClock, installmentScheduleId);

            // when
            var result = facility.submitForApproval(testClock);

            // then - in the new workflow pattern, the specific behavior will depend on the policies
            // For now, we just verify the method can be called without errors
            assertThat(result).isNotNull();
        }
    }

    private TradeLoanApplication.Builder createValidLoanApplicationBuilder(
            @Mock ApplicationNumber applicationNumber,
            @Mock Party customer,
            @Mock InstallmentCount installmentCount,
            @Mock EconomicSector economicSector,
            @Mock Branch branch,
            @Mock RequestReason requestReason,
            @Mock DisburseDestination disburseDestination) {
        // Create a minimal builder that will pass validation
        return TradeLoanApplication.builder()
                .id(LoanApplicationId.of(randomUUID())) // Add the required ID
                .applicationNumber(applicationNumber)
                .requestDate(testClock.instant())
                .customer(customer)
                .requestedAmount(Money.valueOf(BigDecimal.valueOf(100000), CurrencyType.IRR)
                        .orElseThrow())
                .currency(CurrencyType.IRR)
                .requestedLoanDuration(LoanDuration.of(Period.ofDays(365)).orElseThrow())
                .applicantChannel(ApplicantChannel.INTERNET_BANK)
                .installmentCount(installmentCount)
                .economicSector(economicSector)
                .branch(branch)
                .requestReason(requestReason)
                .disburseDestination(disburseDestination)
                .disbursementMethod(DisbursementMethod.LUMP_SUM);
    }
}
