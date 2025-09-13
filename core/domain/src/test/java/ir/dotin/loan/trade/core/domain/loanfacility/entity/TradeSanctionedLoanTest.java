package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import java.math.BigDecimal;
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
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeSanctionedLoan")
@SuppressWarnings("NullAway")
final class TradeSanctionedLoanTest {

    @Mock
    private SanctionSerial mockSanctionSerial;

    private Money validAmount;
    private LoanDuration validDuration;
    private GracePeriod validGracePeriod;
    private InstallmentCount validInstallmentCount;

    @BeforeEach
    void setUp() {
        validAmount =
                Money.valueOf(BigDecimal.valueOf(100000), CurrencyType.IRR).value();
        validDuration = LoanDuration.of(Period.ofDays(365)).orElseThrow();
        validGracePeriod = GracePeriod.of(Period.ofDays(3)).orElseThrow();
        validInstallmentCount = InstallmentCount.of(12).value();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    final class FactoryMethodTests {

        @DisplayName("should reconstitute TradeSanctionedLoan successfully with valid builder")
        @Test
        void shouldReconstituteSuccessfully() {
            // given
            var builder = createValidBuilder();

            // when
            var result = TradeSanctionedLoan.reconstitute(builder);

            // then
            assertThat(result.isSuccess()).isTrue();
            var sanctionedLoan = result.value();
            assertThat(sanctionedLoan).isNotNull();
            assertThat(sanctionedLoan.getId()).isNotNull();
            assertThat(sanctionedLoan.getId()).isInstanceOf(SanctionedLoanId.class);
            assertThat(sanctionedLoan.getSanctionSerial()).isEqualTo(mockSanctionSerial);
            assertThat(sanctionedLoan.getApprovedAmount()).isEqualTo(validAmount);
            assertThat(sanctionedLoan.getLoanDuration()).isEqualTo(validDuration);
        }

        @DisplayName("should fail when builder is null")
        @Test
        void shouldFailWhenBuilderIsNull() {
            // when & then
            assertThatThrownBy(() -> TradeSanctionedLoan.reconstitute(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Builder cannot be null for reconstitution");
        }

        @DisplayName("should fail when required fields are missing")
        @Test
        void shouldFailWhenRequiredFieldsAreMissing() {
            // given
            var builder = TradeSanctionedLoan.newBuilder();

            // when & then
            assertThatThrownBy(() -> TradeSanctionedLoan.reconstitute(builder))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    final class TradeSanctionedLoanDisbursementScheduleBuilderTests {

        @DisplayName("should create new builder instance")
        @Test
        void shouldCreateNewBuilder() {
            // when
            var builder = TradeSanctionedLoan.newBuilder();

            // then
            assertThat(builder).isNotNull().isInstanceOf(TradeSanctionedLoan.TradeSanctionedLoanBuilder.class);
        }

        @DisplayName("should build successfully with all required fields")
        @Test
        void shouldBuildSuccessfullyWithAllRequiredFields() {
            // given
            var builder = createValidBuilder();

            // when
            var result = builder.build();

            // then
            assertThat(result.isSuccess()).isTrue();
            var sanctionedLoan = result.value();
            assertThat(sanctionedLoan).isNotNull().isInstanceOf(TradeSanctionedLoan.class);
        }

        @DisplayName("should validate and return errors for invalid data")
        @Test
        void shouldValidateAndReturnErrorsForInvalidData() {
            // given
            var builder = TradeSanctionedLoan.newBuilder().withId(SanctionedLoanId.of(randomUUID()));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @DisplayName("should support method chaining")
        @Test
        void shouldSupportMethodChaining() {
            // when
            var builder = TradeSanctionedLoan.newBuilder()
                    .withId(SanctionedLoanId.of(randomUUID()))
                    // .withSanction(mockSanction) // Method signature may have changed
                    .withSanctionSerial(mockSanctionSerial)
                    .withApprovedAmount(validAmount)
                    .withLoanDuration(validDuration);

            // then
            assertThat(builder).isNotNull().isInstanceOf(TradeSanctionedLoan.TradeSanctionedLoanBuilder.class);
        }
    }

    @Nested
    @DisplayName("Domain Model Tests")
    final class DomainModelTests {

        @DisplayName("should implement equality correctly")
        @Test
        void shouldImplementEqualityCorrectly() {
            // given
            var id = SanctionedLoanId.of(randomUUID());
            var builder1 = createValidBuilder().withId(id);
            var builder2 = createValidBuilder().withId(id);

            // when
            var sanctionedLoan1 = TradeSanctionedLoan.reconstitute(builder1).value();
            var sanctionedLoan2 = TradeSanctionedLoan.reconstitute(builder2).value();

            // then
            assertThat(sanctionedLoan1).isEqualTo(sanctionedLoan2);
            assertThat(sanctionedLoan1.hashCode()).isEqualTo(sanctionedLoan2.hashCode());
        }

        @DisplayName("should have different identity for different loans")
        @Test
        void shouldHaveDifferentIdentityForDifferentLoans() {
            // given
            var builder1 = createValidBuilder().withId(SanctionedLoanId.of(randomUUID()));
            var builder2 = createValidBuilder().withId(SanctionedLoanId.of(randomUUID()));

            // when
            var sanctionedLoan1 = TradeSanctionedLoan.reconstitute(builder1).value();
            var sanctionedLoan2 = TradeSanctionedLoan.reconstitute(builder2).value();

            // then
            assertThat(sanctionedLoan1.getId()).isNotEqualTo(sanctionedLoan2.getId());
            assertThat(sanctionedLoan1).isNotEqualTo(sanctionedLoan2);
        }

        @DisplayName("should handle null values gracefully in builder")
        @Test
        void shouldHandleNullValuesGracefullyInBuilder() {
            // given
            var builder = TradeSanctionedLoan.newBuilder()
                    .withId(SanctionedLoanId.of(randomUUID()))
                    .withSanctionSerial(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }
    }

    @Nested
    @DisplayName("Inheritance Behavior Tests")
    final class InheritanceBehaviorTests {

        @DisplayName("should properly extend AbstractSanctionedLoan")
        @Test
        void shouldProperlyExtendAbstractSanctionedLoan() {
            // given
            var builder = createValidBuilder();

            // when
            var result = TradeSanctionedLoan.reconstitute(builder);

            // then
            assertThat(result.isSuccess()).isTrue();
            var sanctionedLoan = result.value();

            // Verify inheritance behavior
            assertThat(sanctionedLoan.getSanctionSerial()).isEqualTo(mockSanctionSerial);
            assertThat(sanctionedLoan.getApprovedAmount()).isEqualTo(validAmount);
            assertThat(sanctionedLoan.getLoanDuration()).isEqualTo(validDuration);
            assertThat(sanctionedLoan.getGracePeriod()).isEqualTo(validGracePeriod);
            assertThat(sanctionedLoan.getInstallmentCount()).isEqualTo(validInstallmentCount);
        }

        @DisplayName("should handle basic entity behavior from base class")
        @Test
        void shouldHandleBasicEntityBehaviorFromBaseClass() {
            // given
            var builder = createValidBuilder();

            // when
            var result = TradeSanctionedLoan.reconstitute(builder);

            // then
            assertThat(result.isSuccess()).isTrue();
            var sanctionedLoan = result.value();

            // Verify basic entity capabilities are inherited
            assertThat(sanctionedLoan.getId()).isNotNull();
        }

        @DisplayName("should support entity behavior")
        @Test
        void shouldSupportEntityBehavior() {
            // given
            var builder = createValidBuilder();

            // when
            var result = TradeSanctionedLoan.reconstitute(builder);

            // then
            assertThat(result.isSuccess()).isTrue();
            var sanctionedLoan = result.value();

            // Verify entity capabilities
            assertThat(sanctionedLoan.getId()).isNotNull();
            assertThat(sanctionedLoan.getSanctionSerial()).isEqualTo(mockSanctionSerial);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    final class ValidationTests {

        @DisplayName("should validate required fields")
        @Test
        void shouldValidateRequiredFields() {
            // given
            var builder = TradeSanctionedLoan.newBuilder().withId(SanctionedLoanId.of(randomUUID()));

            // when & then
            assertThatThrownBy(builder::validate)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");
        }

        @DisplayName("should pass validation with all required fields")
        @Test
        void shouldPassValidationWithAllRequiredFields() {
            // given
            var builder = createValidBuilder();

            // when
            var notification = builder.validate();

            // then
            assertThat(notification.hasErrors()).isFalse();
        }
    }

    private TradeSanctionedLoan.TradeSanctionedLoanBuilder createValidBuilder() {
        return TradeSanctionedLoan.newBuilder()
                .withId(SanctionedLoanId.of(randomUUID()))
                .withSanctionSerial(mockSanctionSerial)
                .withApprovedAmount(validAmount)
                .withCurrency(CurrencyType.IRR)
                .withLoanDuration(validDuration)
                .withGracePeriod(validGracePeriod)
                .withInstallmentCount(validInstallmentCount);
    }
}
