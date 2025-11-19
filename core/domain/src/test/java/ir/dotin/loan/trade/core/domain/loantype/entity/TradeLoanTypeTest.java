package ir.dotin.loan.trade.core.domain.loantype.entity;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.commons.core.feature.FeatureConfig;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.EconomicSectorCurrency;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanApplicationStatus;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeLoanType")
@SuppressWarnings("NullAway")
final class TradeLoanTypeTest {

    @Mock
    private FeatureConfig mockFeatureConfig;

    @Mock
    private LoanTypeCode mockLoanTypeCode;

    @Mock
    private Title mockTitle;

    @Mock
    private LoanApplicationStatus mockLoanApplicationStatus;

    @Mock
    private EconomicSectorCurrency mockEconomicSector;

    @Mock
    private LoanTypeGroupId mockGroupId;

    private Set<LoanArrangementId> validArrangementIds;
    private Clock testClock;

    @BeforeEach
    void setUp() {
        // Create distinct arrangement IDs to avoid DistinctVarargsChecker warning
        var arrangementId1 = LoanArrangementId.of(randomUUID());
        var arrangementId2 = LoanArrangementId.of(randomUUID());
        validArrangementIds = ImmutableSet.of(arrangementId1, arrangementId2);
        testClock = Clock.fixed(Instant.parse("2023-12-01T10:00:00Z"), UTC);
    }

    @Nested
    @DisplayName("Factory Method Tests")
    final class FactoryMethodTests {

        @Disabled
        @DisplayName("should create TradeLoanType successfully with valid builder")
        @Test
        void shouldCreateSuccessfully() {
            // given
            var builder = createValidBuilder();

            // when
            var result = TradeLoanType.create(builder, testClock);

            // then
            assertThat(result.isSuccessWithValue()).isTrue();
            var loanType = result.value();
            assertThat(loanType).isNotNull();
            assertThat(loanType.getId()).isNotNull();
            assertThat(loanType.getId()).isInstanceOf(LoanTypeId.class);
            assertThat(loanType.getCode()).isEqualTo(mockLoanTypeCode);
            assertThat(loanType.getTitle()).isEqualTo(mockTitle);
            assertThat(loanType.getLoanArrangementIds()).hasSameElementsAs(validArrangementIds);
            assertThat(loanType.getActive().isActive()).isTrue();
        }

        @DisplayName("should fail when builder is null")
        @Test
        void shouldFailWhenBuilderIsNull() {
            // when & then
            assertThatThrownBy(() -> TradeLoanType.create(null, testClock))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Builder cannot be null for creation");
        }

        @DisplayName("should fail when clock is null")
        @Test
        void shouldFailWhenClockIsNull() {
            // given
            var builder = createValidBuilder();

            // when & then
            assertThatThrownBy(() -> TradeLoanType.create(builder, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Clock cannot be null for creation");
        }

        @Disabled
        @DisplayName("should register creation event when successful")
        @Test
        void shouldRegisterCreationEventWhenSuccessful() {
            // given
            var builder = createValidBuilder();

            // when
            var result = TradeLoanType.create(builder, testClock);

            // then
            assertThat(result.isSuccessWithValue()).isTrue();
        }
    }

    @Nested
    @DisplayName("Reconstitution Tests")
    final class ReconstitutionTests {

        @DisplayName("should reconstitute TradeLoanType successfully with valid builder")
        @Test
        void shouldReconstituteSuccessfully() {
            // given
            var existingId = LoanTypeId.of(randomUUID());
            var builder = createValidBuilder().id(existingId);

            // when
            var loanType = TradeLoanType.reconstitute(builder);

            // then
            assertThat(loanType).isNotNull();
            assertThat(loanType.getId()).isEqualTo(existingId);
            assertThat(loanType.getCode()).isEqualTo(mockLoanTypeCode);
            assertThat(loanType.getLoanArrangementIds()).hasSameElementsAs(validArrangementIds);
        }

        @DisplayName("should fail when builder is null")
        @Test
        void shouldFailWhenBuilderIsNull() {
            // when & then
            assertThatThrownBy(() -> TradeLoanType.reconstitute(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Builder cannot be null for reconstitution");
        }
    }

    @Nested
    @DisplayName("Activation and Deactivation Tests")
    final class ActivationDeactivationTests {

        @DisplayName("should activate loan type successfully")
        @Test
        void shouldActivateSuccessfully() {
            // given
            var builder = createInactiveBuilder();
            var loanType = TradeLoanType.reconstitute(builder);

            // when
            var result = loanType.activate(testClock);

            // then
            assertThat(result.isSuccessWithValue()).isTrue();
            var activatedLoanType = result.orElseThrow();
            assertThat(activatedLoanType.getActive().isActive()).isTrue();
        }

        @DisplayName("should deactivate loan type successfully")
        @Test
        void shouldDeactivateSuccessfully() {
            // given
            var builder = createValidBuilder();
            var loanType = TradeLoanType.reconstitute(builder);

            // when
            var result = loanType.deactivate(testClock);

            // then
            assertThat(result.isSuccessWithValue()).isTrue();
            var deactivatedLoanType = result.orElseThrow();
            assertThat(deactivatedLoanType.getActive().isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    final class TradeSanctionedLoanDisbursementScheduleBuilderTests {

        @DisplayName("should create new builder with feature config")
        @Test
        void shouldCreateNewBuilderWithFeatureConfig() {
            // when
            var builder = TradeLoanType.builder();

            // then
            assertThat(builder).isNotNull().isInstanceOf(TradeLoanType.Builder.class);
        }

        @DisplayName("should validate successfully with valid data")
        @Test
        void shouldValidateSuccessfullyWithValidData() {
            // given
            var builder = createValidBuilder();

            // when
            var notification = builder.validate();

            // then
            assertThat(notification.hasErrors()).isFalse();
        }
    }

    @Nested
    @DisplayName("Domain Model Tests")
    final class DomainModelTests {

        @DisplayName("should return immutable loan arrangement IDs")
        @Test
        void shouldReturnImmutableLoanArrangementIds() {
            // given
            var builder = createValidBuilder();
            var loanType = TradeLoanType.reconstitute(builder);

            // when
            var arrangementIds = loanType.getLoanArrangementIds();

            // then
            LoanArrangementId generate = LoanArrangementId.of(randomUUID());
            //noinspection DataFlowIssue
            assertThatThrownBy(() -> arrangementIds.add(generate)).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("should maintain proper state validation")
        @Test
        void shouldMaintainProperStateValidation() {
            // given
            var builder = createValidBuilder();

            // when & then - should not throw any exceptions
            var loanType = TradeLoanType.reconstitute(builder);
            assertThat(loanType).isNotNull();
        }
    }

    private TradeLoanType.Builder createValidBuilder() {
        return TradeLoanType.builder()
                .id(LoanTypeId.of(randomUUID()))
                .code(mockLoanTypeCode)
                .title(mockTitle)
                .gatewayType(GatewayType.LOAN)
                .loanApplicationAllowed(mockLoanApplicationStatus)
                .segmentType(SegmentType.LOAN)
                .economicSectorCurrencies(ImmutableSet.of(mockEconomicSector))
                .groupId(mockGroupId)
                .loanArrangementIds(validArrangementIds);
    }

    private TradeLoanType.Builder createInactiveBuilder() {
        return createValidBuilder()
                .active(new ir.dotin.loan.baseloan.core.domain.shared.vo.Active(false))
                .disable(new ir.dotin.loan.baseloan.core.domain.shared.vo.Disable(false));
    }
}
