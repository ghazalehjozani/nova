package ir.dotin.loan.trade.core.domain.loanfacility.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.vo.CurrencyType;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeLoanFacilityService")
@SuppressWarnings("NullAway")
class TradeLoanFacilityServiceTest {

    @Mock
    private TradeLoanFacility mockFacility;

    @Mock
    private TradeLoanApplication mockLoanApplication;

    @Mock
    private CollateralSerial mockCollateralSerial;

    private TradeLoanFacilityService service;
    private List<TransactionNumber> validTradeTransactionNumbers;

    @BeforeEach
    void setUp() {
        Clock testClock = Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), UTC);
        service = new TradeLoanFacilityService(testClock);

        validTradeTransactionNumbers = ImmutableList.of(
                TransactionNumber.of("TRD-001").orElseThrow(),
                TransactionNumber.of("TRD-002").orElseThrow());
    }

    @DisplayName("when validating transaction numbers")
    @Nested
    final class ValidateTransactionNumbersTests {

        @DisplayName("should succeed when transaction numbers are valid trade format")
        @Test
        void shouldSucceedWhenTransactionNumbersAreValidTradeFormat() {
            // when
            var result = service.validateTransactionNumbers(mockFacility, validTradeTransactionNumbers);

            // then
            assertThat(result.isSuccess()).isTrue();
        }

        @DisplayName("should fail when transaction numbers are null")
        @Test
        void shouldFailWhenTransactionNumbersAreNull() {
            // when
            var result = service.validateTransactionNumbers(mockFacility, null);

            // then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.notification().hasErrors()).isTrue();
        }

        @DisplayName("should fail when transaction numbers are empty")
        @Test
        void shouldFailWhenTransactionNumbersAreEmpty() {
            // when
            var result = service.validateTransactionNumbers(mockFacility, ImmutableList.of());

            // then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.notification().hasErrors()).isTrue();
        }
    }

    @DisplayName("when checking if collateral can be added")
    @Nested
    final class CanAddCollateralTests {

        @DisplayName("should allow collateral when facility is approved")
        @Test
        void shouldAllowCollateralWhenFacilityIsApproved() {
            // given
            when(mockFacility.getCurrentState()).thenReturn(FacilityStatus.APPROVED);

            // when
            var canAdd = service.canAddCollateral(mockFacility, mockCollateralSerial);

            // then
            assertThat(canAdd).isTrue();
        }

        @DisplayName("should allow collateral when contract is issued")
        @Test
        void shouldAllowCollateralWhenContractIsIssued() {
            // given
            when(mockFacility.getCurrentState()).thenReturn(FacilityStatus.ISSUE_CONTRACT);

            // when
            var canAdd = service.canAddCollateral(mockFacility, mockCollateralSerial);

            // then
            assertThat(canAdd).isTrue();
        }

        @DisplayName("should not allow collateral when facility is pending")
        @Test
        void shouldNotAllowCollateralWhenFacilityIsPending() {
            // given
            when(mockFacility.getCurrentState()).thenReturn(FacilityStatus.PENDING_APPROVAL);

            // when
            var canAdd = service.canAddCollateral(mockFacility, mockCollateralSerial);

            // then
            assertThat(canAdd).isFalse();
        }
    }

    @DisplayName("when checking if facility can be cancelled")
    @Nested
    final class CanCancelTests {

        @DisplayName("should allow cancellation when facility is pending approval")
        @Test
        void shouldAllowCancellationWhenFacilityIsPendingApproval() {
            // given
            when(mockFacility.getCurrentState()).thenReturn(FacilityStatus.PENDING_APPROVAL);

            // when
            var canCancel = service.canCancel(mockFacility);

            // then
            assertThat(canCancel).isTrue();
        }

        @DisplayName("should allow cancellation when facility is approved")
        @Test
        void shouldAllowCancellationWhenFacilityIsApproved() {
            // given
            when(mockFacility.getCurrentState()).thenReturn(FacilityStatus.APPROVED);

            // when
            var canCancel = service.canCancel(mockFacility);

            // then
            assertThat(canCancel).isTrue();
        }

        @DisplayName("should not allow cancellation when facility is active")
        @Test
        void shouldNotAllowCancellationWhenFacilityIsActive() {
            // given
            when(mockFacility.getCurrentState()).thenReturn(FacilityStatus.ACTIVE);

            // when
            var canCancel = service.canCancel(mockFacility);

            // then
            assertThat(canCancel).isFalse();
        }

        @DisplayName("should not allow cancellation when facility is closed paid off")
        @Test
        void shouldNotAllowCancellationWhenFacilityIsClosedPaidOff() {
            // given
            when(mockFacility.getCurrentState()).thenReturn(FacilityStatus.CLOSED_PAID_OFF);

            // when
            var canCancel = service.canCancel(mockFacility);

            // then
            assertThat(canCancel).isFalse();
        }

        @DisplayName("should not allow cancellation when facility is closed defaulted")
        @Test
        void shouldNotAllowCancellationWhenFacilityIsClosedDefaulted() {
            // given
            when(mockFacility.getCurrentState()).thenReturn(FacilityStatus.CLOSED_DEFAULTED);

            // when
            var canCancel = service.canCancel(mockFacility);

            // then
            assertThat(canCancel).isFalse();
        }
    }

    @DisplayName("when verifying zero balance")
    @Nested
    final class VerifyZeroBalanceTests {

        @DisplayName("should return success")
        @Test
        void shouldReturnSuccess() {
            // when
            var result = service.verifyZeroBalance(mockFacility);

            // then
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @DisplayName("when verifying default conditions")
    @Nested
    final class VerifyDefaultConditionsTests {

        @DisplayName("should return success")
        @Test
        void shouldReturnSuccess() {
            // when
            var result = service.verifyDefaultConditions(mockFacility);

            // then
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @DisplayName("when creating sanctioned loan from application")
    @Nested
    final class CreateSanctionedLoanFromApplicationTests {

        @DisplayName("should create valid trade sanctioned loan builder")
        @Test
        void shouldCreateValidTradeSanctionedLoanBuilder() {
            // given
            var requestedAmount =
                    Money.valueOf(new BigDecimal("1000.00"), CurrencyType.USD).orElseThrow();
            var currency = CurrencyType.USD;
            var gracePeriod = GracePeriod.of(Duration.ofDays(30)).orElseThrow();
            var installmentCount = InstallmentCount.of(12).orElseThrow();
            var loanDuration = LoanDuration.of(Duration.ofDays(365)).orElseThrow();

            when(mockLoanApplication.getRequestedAmount()).thenReturn(requestedAmount);
            when(mockLoanApplication.getCurrency()).thenReturn(currency);
            when(mockLoanApplication.getGracePeriod()).thenReturn(gracePeriod);
            when(mockLoanApplication.getInstallmentCount()).thenReturn(installmentCount);
            when(mockLoanApplication.getRequestedLoanDuration()).thenReturn(loanDuration);

            // when
            Result<TradeSanctionedLoan.TradeSanctionedLoanBuilder> result =
                    service.createSanctionedLoanFromApplication(mockLoanApplication);

            // then
            assertThat(result.isSuccess()).isTrue();

            var builder = result.orElseThrow();
            assertThat(builder).isInstanceOf(TradeSanctionedLoan.TradeSanctionedLoanBuilder.class);

            // Build and verify the sanctioned loan
            var sanctionedLoanResult = builder.build();
            assertThat(sanctionedLoanResult.isSuccess()).isTrue();

            var sanctionedLoan = sanctionedLoanResult.orElseThrow();
            assertThat(sanctionedLoan.getApprovedAmount()).isEqualTo(requestedAmount);
            assertThat(sanctionedLoan.getCurrency()).isEqualTo(currency);
            assertThat(sanctionedLoan.getGracePeriod()).isEqualTo(gracePeriod);
            assertThat(sanctionedLoan.getInstallmentCount()).isEqualTo(installmentCount);
            assertThat(sanctionedLoan.getLoanDuration()).isEqualTo(loanDuration);
            assertThat(sanctionedLoan.getId()).isNotNull();
            assertThat(sanctionedLoan.getSanctionSerial()).isNotNull();
        }
    }
}
