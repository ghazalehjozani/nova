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
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            var sanctionedLoan = assertDoesNotThrow(() -> builder.build());

            // then
            assertThat(sanctionedLoan).isNotNull();
            assertThat(sanctionedLoan.getId()).isNotNull();
            assertThat(sanctionedLoan.getId()).isInstanceOf(SanctionedLoanId.class);
            assertThat(sanctionedLoan.getSanctionSerial()).isEqualTo(mockSanctionSerial);
            assertThat(sanctionedLoan.getApprovedAmount()).isEqualTo(validAmount);
            assertThat(sanctionedLoan.getLoanDuration()).isEqualTo(validDuration);
        }

        @DisplayName("should fail when required fields are missing")
        @Test
        void shouldFailWhenRequiredFieldsAreMissing() {
            // given
            var builder = TradeSanctionedLoan.builder();

            // when & then
            assertThrows(IllegalArgumentException.class, () -> builder.build());
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    final class TradeSanctionedLoanDisbursementScheduleBuilderTests {

        @DisplayName("should create new builder instance")
        @Test
        void shouldCreateNewBuilder() {
            // when
            var builder = TradeSanctionedLoan.builder();

            // then
            assertThat(builder).isNotNull().isInstanceOf(TradeSanctionedLoan.Builder.class);
        }

        @DisplayName("should build successfully with all required fields")
        @Test
        void shouldBuildSuccessfullyWithAllRequiredFields() {
            // given
            var builder = createValidBuilder();

            // when
            var sanctionedLoan = assertDoesNotThrow(() -> builder.build());

            // then
            assertThat(sanctionedLoan).isNotNull().isInstanceOf(TradeSanctionedLoan.class);
        }

        @DisplayName("should validate and return errors for invalid data")
        @Test
        void shouldValidateAndReturnErrorsForInvalidData() {
            // given
            var builder = TradeSanctionedLoan.builder().id(SanctionedLoanId.of(randomUUID()));

            // when & then
            assertThrows(IllegalArgumentException.class, () -> builder.build());
        }

        @DisplayName("should support method chaining")
        @Test
        void shouldSupportMethodChaining() {
            // when
            var builder = TradeSanctionedLoan.builder()
                    .id(SanctionedLoanId.of(randomUUID()))
                    // .withSanction(mockSanction) // Method signature may have changed
                    .sanctionSerial(mockSanctionSerial)
                    .approvedAmount(validAmount)
                    .loanDuration(validDuration);

            // then
            assertThat(builder).isNotNull().isInstanceOf(TradeSanctionedLoan.Builder.class);
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
            var builder1 = createValidBuilder().id(id);
            var builder2 = createValidBuilder().id(id);

            // when
            var sanctionedLoan1 = assertDoesNotThrow(() -> builder1.build());
            var sanctionedLoan2 = assertDoesNotThrow(() -> builder2.build());

            // then
            assertThat(sanctionedLoan1).isEqualTo(sanctionedLoan2);
            assertThat(sanctionedLoan1.hashCode()).isEqualTo(sanctionedLoan2.hashCode());
        }

        @DisplayName("should have different identity for different loans")
        @Test
        void shouldHaveDifferentIdentityForDifferentLoans() {
            // given
            var builder1 = createValidBuilder().id(SanctionedLoanId.of(randomUUID()));
            var builder2 = createValidBuilder().id(SanctionedLoanId.of(randomUUID()));

            // when
            var sanctionedLoan1 = assertDoesNotThrow(() -> builder1.build());
            var sanctionedLoan2 = assertDoesNotThrow(() -> builder2.build());

            // then
            assertThat(sanctionedLoan1.getId()).isNotEqualTo(sanctionedLoan2.getId());
            assertThat(sanctionedLoan1).isNotEqualTo(sanctionedLoan2);
        }

        @DisplayName("should handle null values gracefully in builder")
        @Test
        void shouldHandleNullValuesGracefullyInBuilder() {
            // given
            var builder = TradeSanctionedLoan.builder()
                    .id(SanctionedLoanId.of(randomUUID()))
                    .sanctionSerial(null);

            // when & then
            assertThrows(IllegalArgumentException.class, () -> builder.build());
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
            var sanctionedLoan = assertDoesNotThrow(() -> builder.build());

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
            var sanctionedLoan = assertDoesNotThrow(() -> builder.build());

            // Verify basic entity capabilities are inherited
            assertThat(sanctionedLoan.getId()).isNotNull();
        }

        @DisplayName("should support entity behavior")
        @Test
        void shouldSupportEntityBehavior() {
            // given
            var builder = createValidBuilder();

            // when
            var sanctionedLoan = assertDoesNotThrow(() -> builder.build());

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
            var builder = TradeSanctionedLoan.builder().id(SanctionedLoanId.of(randomUUID()));

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

    private TradeSanctionedLoan.Builder createValidBuilder() {
        return TradeSanctionedLoan.builder()
                .id(SanctionedLoanId.of(randomUUID()))
                .sanctionSerial(mockSanctionSerial)
                .approvedAmount(validAmount)
                .currency(CurrencyType.IRR)
                .loanDuration(validDuration)
                .gracePeriod(validGracePeriod)
                .installmentCount(validInstallmentCount)
                .disbursementMethod(DisbursementMethod.LUMP_SUM);
    }
}
