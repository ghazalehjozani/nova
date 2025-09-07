package ir.dotin.loan.trade.core.domain.loanarrangement.entity;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.GracePeriodPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.PenaltyPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RegulatoryCompliancePolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RepaymentPriorityPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.domain.loanarrangement.event.TradeLoanArrangementCreated;
import ir.dotin.platform.domain.common.feature.FeatureConfig;
import ir.dotin.platform.domain.common.vo.CurrencyType;
import ir.dotin.platform.domain.common.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeLoanArrangement Aggregate")
@SuppressWarnings({"NullAway", "unchecked"})
class TradeLoanArrangementTest {

    private Clock testClock;

    private TradeLoanArrangement.Builder builder;

    @Mock
    private FeatureConfig mockFeatureConfig;

    @BeforeEach
    void setUp() {
        builder = TradeLoanArrangement.newBuilder(mockFeatureConfig);
        testClock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), UTC);
    }

    @DisplayName("when creating new arrangement")
    @Nested
    final class CreateArrangementTests {

        @Test
        @DisplayName("should create successfully and register created event")
        void shouldCreateSuccessfullyWithValidBuilder(
                @Mock FeatureConfig featureConfig,
                @Mock InterestPolicy interestPolicy,
                @Mock PenaltyPolicy penaltyPolicy,
                @Mock InstallmentPolicy installmentPolicy,
                @Mock GracePeriodPolicy gracePeriodPolicy,
                @Mock RepaymentPriorityPolicy repaymentPriorityPolicy,
                @Mock RegulatoryCompliancePolicy regulatoryCompliancePolicy,
                @Mock CollateralPolicy collateralPolicy) {
            // given
            var validBuilder = createValidBuilder(
                    featureConfig,
                    interestPolicy,
                    penaltyPolicy,
                    installmentPolicy,
                    gracePeriodPolicy,
                    repaymentPriorityPolicy,
                    regulatoryCompliancePolicy,
                    collateralPolicy);

            // when
            var result = TradeLoanArrangement.create(validBuilder, testClock);

            // then
            assertThat(result.isSuccess()).isTrue();
            var arrangement = result.value();
            assertThat(arrangement).isNotNull();
            assertThat(arrangement.getId()).isNotNull();
            assertThat(arrangement.getPreviousVersion()).isNull();
            assertThat(arrangement.domainEvents()).hasSize(1);
            assertThat(arrangement.domainEvents().getFirst()).isInstanceOf(TradeLoanArrangementCreated.class);
        }
    }

    @DisplayName("when reconstituting arrangement")
    @Nested
    final class ReconstituteArrangementTests {

        @Test
        @DisplayName("should reconstitute successfully from a complete builder")
        void shouldReconstituteSuccessfully(
                @Mock FeatureConfig featureConfig,
                @Mock InterestPolicy interestPolicy,
                @Mock PenaltyPolicy penaltyPolicy,
                @Mock InstallmentPolicy installmentPolicy,
                @Mock GracePeriodPolicy gracePeriodPolicy,
                @Mock RepaymentPriorityPolicy repaymentPriorityPolicy,
                @Mock RegulatoryCompliancePolicy regulatoryCompliancePolicy,
                @Mock CollateralPolicy collateralPolicy
        ) {
            // given: A builder representing data from a persistent source
            var id = LoanArrangementId.of(randomUUID());
            var validBuilder = createValidBuilder(
                    featureConfig,
                    interestPolicy,
                    penaltyPolicy,
                    installmentPolicy,
                    gracePeriodPolicy,
                    repaymentPriorityPolicy,
                    regulatoryCompliancePolicy,
                    collateralPolicy)
                    .withId(id)
                    .withActive(new Active(true));

            // when
            var arrangement = TradeLoanArrangement.reconstitute(validBuilder);

            // then
            assertThat(arrangement).isNotNull();
            assertThat(arrangement.getId()).isEqualTo(id);
            assertThat(arrangement.getCode().value()).isEqualTo("TRD-ARR-01");
            assertThat(arrangement.domainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {

        @Test
        @DisplayName("should throw exception when feature config is null")
        void shouldThrowExceptionWhenFeatureConfigIsNull() {
            assertThatThrownBy(() -> TradeLoanArrangement.newBuilder(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Create Method Tests")
    final class CreateMethodTests {

        @Test
        @DisplayName("should throw exception when clock is null")
        void shouldThrowExceptionWhenClockIsNull() {
            assertThatThrownBy(() -> TradeLoanArrangement.create(builder, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Clock cannot be null for creation");
        }

        @Test
        @DisplayName("should throw exception when builder is null")
        void shouldThrowExceptionWhenBuilderIsNull() {
            assertThatThrownBy(() -> TradeLoanArrangement.create(null, testClock))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Builder cannot be null for creation");
        }
    }

    private TradeLoanArrangement.Builder createValidBuilder(
            FeatureConfig featureConfig,
            InterestPolicy interestPolicy,
            PenaltyPolicy penaltyPolicy,
            InstallmentPolicy installmentPolicy,
            GracePeriodPolicy gracePeriodPolicy,
            RepaymentPriorityPolicy repaymentPriorityPolicy,
            RegulatoryCompliancePolicy regulatoryCompliancePolicy,
            CollateralPolicy collateralPolicy
    ) {
        return TradeLoanArrangement.newBuilder(featureConfig)
                .withCode(new LoanArrangementCode("TRD-ARR-01"))
                .withTitle(new Title("Default Trade Arrangement"))
                .withCurrencies(ImmutableSet.of(CurrencyType.IRR))
                .withAmountRange(Range.closed(
                        Money.valueOf(BigDecimal.valueOf(1000), CurrencyType.IRR).orElseThrow(),
                        Money.valueOf(BigDecimal.valueOf(100000), CurrencyType.IRR).orElseThrow()
                ))
                .withDurationRange(Range.closed(Duration.ofDays(30), Duration.ofDays(365)))
                .withGuarantorCount(1)
                .withCustomerType(PartyType.LEGAL)
                .withHasInstallmentCard(false)
                .withLifeInsurancePaymentType(LifeInsurancePaymentType.NONE)
                .withLoanSecondaryType(LoanSecondaryType.GENERAL)
                .withSectionType(SectionType.CURRENT)
                .withAutoApproval(true)
                .withDisbursementMethod(DisbursementMethod.LUMP_SUMP)
                .withInterestPolicy(interestPolicy)
                .withPenaltyPolicy(penaltyPolicy)
                .withInstallmentPolicy(installmentPolicy)
                .withGracePeriodPolicy(gracePeriodPolicy)
                .withRepaymentPriorityPolicy(repaymentPriorityPolicy)
                .withRegulatoryCompliancePolicy(regulatoryCompliancePolicy)
                .withCollateralPolicy(collateralPolicy);
    }
}