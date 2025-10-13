package ir.dotin.loan.trade.core.domain.shared;

import java.lang.reflect.Method;
import java.time.Period;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("TradeLoanParameterProvider Test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
final class TradeLoanParameterProviderTest {

    @Nested
    @DisplayName("Contract Tests")
    final class ContractTests {

        @ParameterizedTest
        @MethodSource("providerMethodContract")
        @DisplayName("should have correctly defined methods")
        void shouldHaveCorrectlyDefinedMethods(String methodName, Class<?> expectedReturnType)
                throws NoSuchMethodException {
            // Arrange & Act
            Method method = TradeLoanParameterProvider.class.getMethod(methodName);

            // Assert
            assertThat(method.getReturnType()).isEqualTo(expectedReturnType);
        }

        private static Stream<Arguments> providerMethodContract() {
            return Stream.of(
                    arguments("getApprovedAmount", Money.class),
                    arguments("getRequestedAmount", Money.class),
                    arguments("getGracePeriod", Period.class),
                    arguments("getCommissionAmount", Money.class),
                    arguments("getShipmentValue", Money.class),
                    arguments("getInsuranceRate", Rate.class));
        }
    }

    @Nested
    @DisplayName("Mock Implementation Tests")
    @SuppressWarnings("NullAway")
    final class MockImplementationTests {

        @Mock
        private TradeLoanParameterProvider mockParameterProvider;

        @Test
        @DisplayName("should return approved amount from mock")
        void shouldReturnApprovedAmountFromMock(@Mock Money mockApprovedAmount) {
            // Arrange
            given(mockParameterProvider.getApprovedAmount()).willReturn(mockApprovedAmount);

            // Act
            Money result = mockParameterProvider.getApprovedAmount();

            // Assert
            assertThat(result).isEqualTo(mockApprovedAmount);
            then(mockParameterProvider).should().getApprovedAmount();
        }

        @Test
        @DisplayName("should return requested amount from mock")
        void shouldReturnRequestedAmountFromMock(@Mock Money mockRequestedAmount) {
            // Arrange
            given(mockParameterProvider.getRequestedAmount()).willReturn(mockRequestedAmount);

            // Act
            Money result = mockParameterProvider.getRequestedAmount();

            // Assert
            assertThat(result).isEqualTo(mockRequestedAmount);
            then(mockParameterProvider).should().getRequestedAmount();
        }

        @Test
        @DisplayName("should return grace period from mock")
        void shouldReturnGracePeriodFromMock(@Mock Period mockGracePeriod) {
            // Arrange
            given(mockParameterProvider.getGracePeriod()).willReturn(mockGracePeriod);

            // Act
            Period result = mockParameterProvider.getGracePeriod();

            // Assert
            assertThat(result).isEqualTo(mockGracePeriod);
            then(mockParameterProvider).should().getGracePeriod();
        }

        @Test
        @DisplayName("should return commission amount from mock")
        void shouldReturnCommissionAmountFromMock(@Mock Money mockCommissionAmount) {
            // Arrange
            given(mockParameterProvider.getCommissionAmount()).willReturn(mockCommissionAmount);

            // Act
            Money result = mockParameterProvider.getCommissionAmount();

            // Assert
            assertThat(result).isEqualTo(mockCommissionAmount);
            then(mockParameterProvider).should().getCommissionAmount();
        }

        @Test
        @DisplayName("should return shipment number from mock")
        void shouldReturnShipmentValueFromMock(@Mock Money mockShipmentValue) {
            // Arrange
            given(mockParameterProvider.getShipmentValue()).willReturn(mockShipmentValue);

            // Act
            Money result = mockParameterProvider.getShipmentValue();

            // Assert
            assertThat(result).isEqualTo(mockShipmentValue);
            then(mockParameterProvider).should().getShipmentValue();
        }

        @Test
        @DisplayName("should return insurance rate from mock")
        void shouldReturnInsuranceRateFromMock(@Mock Rate mockInsuranceRate) {
            // Arrange
            given(mockParameterProvider.getInsuranceRate()).willReturn(mockInsuranceRate);

            // Act
            Rate result = mockParameterProvider.getInsuranceRate();

            // Assert
            assertThat(result).isEqualTo(mockInsuranceRate);
            then(mockParameterProvider).should().getInsuranceRate();
        }
    }
}
