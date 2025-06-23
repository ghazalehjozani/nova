package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.domain.common.vo.CurrencyType;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.*;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCreatedEvent;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanApplicationId;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeLoanFacility")
@SuppressWarnings("NullAway")
class TradeLoanFacilityTest {

    private Clock testClock;
    private TradeLoanArrangementId loanArrangementId;
    private TradeLoanFacilityFactory factory;

    @BeforeEach
    void setUp() {
        testClock = Clock.fixed(Instant.parse("2023-12-01T10:00:00Z"), UTC);
        loanArrangementId = TradeLoanArrangementId.generate();
        factory = new TradeLoanFacilityFactory(testClock);
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
                @Mock DisburseDestination disburseDestination) {
            // given
            var applicationBuilder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = applicationBuilder.build().orElseThrow();
            var facilityId = TradeLoanFacilityId.generate();

            // when
            var facility = factory.create(facilityId, application, loanArrangementId);

            // then
            assertThat(facility).isNotNull();
            assertThat(facility.getLoanArrangementId()).isEqualTo(loanArrangementId);
            assertThat(facility.getCurrentState()).isEqualTo(FacilityStatus.APPLICATION_SUBMITTED);
            assertThat(facility.getLoanFacilityType()).isEqualTo("TRADE");
        }

        @DisplayName("should fail when facility ID is null")
        @Test
        void shouldFailWhenFacilityIdIsNull(@Mock TradeLoanApplication application) {
            // when & then
            assertThatThrownBy(() -> factory.create(null, application, loanArrangementId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Facility ID cannot be null");
        }

        @DisplayName("should fail when application is null")
        @Test
        void shouldFailWhenApplicationIsNull() {
            // given
            var facilityId = TradeLoanFacilityId.generate();

            // when & then
            assertThatThrownBy(() -> factory.create(facilityId, null, loanArrangementId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Application cannot be null");
        }

        @DisplayName("should fail when loan arrangement id is null")
        @Test
        void shouldFailWhenLoanArrangementIdIsNull(@Mock TradeLoanApplication application) {
            // given
            var facilityId = TradeLoanFacilityId.generate();

            // when & then
            assertThatThrownBy(() -> factory.create(facilityId, application, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Loan arrangement ID cannot be null");
        }

        @DisplayName("should fail when clock in factory constructor is null")
        @Test
        void shouldFailWhenClockInFactoryConstructorIsNull() {
            // when & then
            assertThatThrownBy(() -> new TradeLoanFacilityFactory(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Clock cannot be null");
        }

        @DisplayName("should create factory with valid clock")
        @Test
        void shouldCreateFactoryWithValidClock() {
            // when
            var factoryWithClock = new TradeLoanFacilityFactory(testClock);

            // then - factory should be created successfully
            assertThat(factoryWithClock).isNotNull();
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
                @Mock DisburseDestination disburseDestination) {
            // given
            var applicationBuilder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = applicationBuilder.build().orElseThrow();
            var facilityId = TradeLoanFacilityId.generate();

            // when
            var facility = factory.create(facilityId, application, loanArrangementId);

            // then
            assertThat(facility).isNotNull();
            var events = facility.domainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(TradeLoanFacilityCreatedEvent.class);

            var createdEvent = (TradeLoanFacilityCreatedEvent) events.getFirst();
            assertThat(createdEvent.aggregateId()).isEqualTo(facility.getId());
            assertThat(createdEvent.payload().applicationId())
                    .isEqualTo(facility.getLoanApplication().getId());
            assertThat(createdEvent.payload().customer()).isEqualTo(customer);
        }
    }

    @DisplayName("when reconstituting trade loan facility")
    @Nested
    final class ReconstituteTradeLoanFacilityTests {

        @DisplayName("should reconstitute facility with all parameters")
        @Test
        void shouldReconstituteFacilityWithAllParameters(
                @Mock TradeLoanApplication application, @Mock TradeSanctionedLoan sanctionedLoan) {
            // given
            var facilityId = TradeLoanFacilityId.generate();
            var status = FacilityStatus.ACTIVE;

            // when
            var facility = factory.reconstitute(facilityId, application, sanctionedLoan, status, loanArrangementId);

            // then
            assertThat(facility).isNotNull();
            assertThat(facility.getId()).isEqualTo(facilityId);
            assertThat(facility.getLoanArrangementId()).isEqualTo(loanArrangementId);
            assertThat(facility.getCurrentState()).isEqualTo(status);
            assertThat(facility.getSanctionedLoan()).hasValue(sanctionedLoan);
        }

        @DisplayName("should reconstitute facility without sanctioned loan")
        @Test
        void shouldReconstituteFacilityWithoutSanctionedLoan(@Mock TradeLoanApplication application) {
            // given
            var facilityId = TradeLoanFacilityId.generate();
            var status = FacilityStatus.PENDING_APPROVAL;

            // when
            var facility = factory.reconstitute(facilityId, application, null, status, loanArrangementId);

            // then
            assertThat(facility).isNotNull();
            assertThat(facility.getSanctionedLoan()).isEmpty();
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
                @Mock DisburseDestination disburseDestination) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = builder.build().orElseThrow();
            var facilityId = TradeLoanFacilityId.generate();
            TradeLoanFacility facility = factory.create(facilityId, application, loanArrangementId);

            // when
            var returnedLoanTypeId = facility.getLoanTypeId();

            // then
            assertThat(returnedLoanTypeId).isNotNull().isInstanceOf(TradeLoanTypeId.class);
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
                @Mock DisburseDestination disburseDestination) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = builder.build().orElseThrow();
            var facilityId = TradeLoanFacilityId.generate();
            TradeLoanFacility facility = factory.create(facilityId, application, loanArrangementId);

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
                @Mock DisburseDestination disburseDestination) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = builder.build().orElseThrow();
            var facilityId = TradeLoanFacilityId.generate();
            TradeLoanFacility facility = factory.create(facilityId, application, loanArrangementId);

            // when
            var facilityType = facility.getLoanFacilityType();

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
                @Mock DisburseDestination disburseDestination) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = builder.build().orElseThrow();
            var facilityId = TradeLoanFacilityId.generate();
            TradeLoanFacility facility = factory.create(facilityId, application, loanArrangementId);

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
                @Mock DisburseDestination disburseDestination) {
            // given
            var builder = createValidLoanApplicationBuilder(
                    applicationNumber,
                    customer,
                    installmentCount,
                    economicSector,
                    branch,
                    requestReason,
                    disburseDestination);
            var application = builder.build().orElseThrow();
            var facilityId = TradeLoanFacilityId.generate();
            TradeLoanFacility facility = factory.create(facilityId, application, loanArrangementId);

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
        return TradeLoanApplication.newBuilder()
                .withId(TradeLoanApplicationId.generate()) // Add the required ID
                .withApplicationNumber(applicationNumber)
                .withRequestDate(testClock.instant())
                .withCustomer(customer)
                .withRequestedAmount(Money.valueOf(BigDecimal.valueOf(100000), CurrencyType.IRR)
                        .orElseThrow())
                .withCurrency(CurrencyType.IRR)
                .withRequestedLoanDuration(
                        LoanDuration.of(java.time.Duration.ofDays(365)).orElseThrow())
                .withApplicantChannel(ApplicantChannel.INTERNET_BANK)
                .withInstallmentCount(installmentCount)
                .withEconomicSector(economicSector)
                .withBranch(branch)
                .withRequestReason(requestReason)
                .withDisburseDestination(disburseDestination);
    }
}
