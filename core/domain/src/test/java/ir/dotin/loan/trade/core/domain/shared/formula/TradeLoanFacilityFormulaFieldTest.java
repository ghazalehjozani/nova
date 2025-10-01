package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.platform.commons.domain.vo.ValueType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeLoanFacilityFormulaField Enum Test")
@SuppressWarnings("NullAway")
final class TradeLoanFacilityFormulaFieldTest {

    @Mock
    private TradeLoanParameterProvider mockProvider;

    @ParameterizedTest
    @EnumSource(TradeLoanFacilityFormulaField.class)
    @DisplayName("should return the correct expected ValueType for each field")
    void shouldReturnCorrectExpectedType(TradeLoanFacilityFormulaField field) {
        // Arrange
        ValueType expectedType =
                switch (field) {
                    case APPROVED_AMOUNT, REQUESTED_AMOUNT, COMMISSION_AMOUNT, SHIPMENT_VALUE -> ValueType.MONEY;
                    case INSURANCE_RATE -> ValueType.RATE;
                    default -> throw new IllegalStateException("Unexpected number: " + field);
                };

        // Act
        ValueType actualType = field.getExpectedType();

        // Assert
        assertThat(actualType).isEqualTo(expectedType);
    }

    @Test
    @DisplayName("extractor for APPROVED_AMOUNT should call getApprovedAmount")
    void extractor_shouldCallGetApprovedAmount(@Mock Money expectedMoney) {
        // Arrange
        when(mockProvider.getApprovedAmount()).thenReturn(expectedMoney);
        Function<TradeLoanParameterProvider, Object> extractor =
                TradeLoanFacilityFormulaField.APPROVED_AMOUNT.getExtractor();

        // Act
        Object result = extractor.apply(mockProvider);

        // Assert
        verify(mockProvider).getApprovedAmount();
        assertThat(result).isSameAs(expectedMoney);
    }

    @Test
    @DisplayName("extractor for REQUESTED_AMOUNT should call getRequestedAmount")
    void extractor_shouldCallGetRequestedAmount(@Mock Money expectedMoney) {
        // Arrange
        when(mockProvider.getRequestedAmount()).thenReturn(expectedMoney);
        Function<TradeLoanParameterProvider, Object> extractor =
                TradeLoanFacilityFormulaField.REQUESTED_AMOUNT.getExtractor();

        // Act
        Object result = extractor.apply(mockProvider);

        // Assert
        verify(mockProvider).getRequestedAmount();
        assertThat(result).isSameAs(expectedMoney);
    }

    @Test
    @DisplayName("extractor for COMMISSION_AMOUNT should call getCommissionAmount")
    void extractor_shouldCallGetCommissionAmount(@Mock Money expectedMoney) {
        // Arrange
        when(mockProvider.getCommissionAmount()).thenReturn(expectedMoney);
        Function<TradeLoanParameterProvider, Object> extractor =
                TradeLoanFacilityFormulaField.COMMISSION_AMOUNT.getExtractor();

        // Act
        Object result = extractor.apply(mockProvider);

        // Assert
        verify(mockProvider).getCommissionAmount();
        assertThat(result).isSameAs(expectedMoney);
    }

    @Test
    @DisplayName("extractor for SHIPMENT_VALUE should call getShipmentValue")
    void extractor_shouldCallGetShipmentValue(@Mock Money expectedMoney) {
        // Arrange
        when(mockProvider.getShipmentValue()).thenReturn(expectedMoney);
        Function<TradeLoanParameterProvider, Object> extractor =
                TradeLoanFacilityFormulaField.SHIPMENT_VALUE.getExtractor();

        // Act
        Object result = extractor.apply(mockProvider);

        // Assert
        verify(mockProvider).getShipmentValue();
        assertThat(result).isSameAs(expectedMoney);
    }

    @Test
    @DisplayName("extractor for INSURANCE_RATE should call getInsuranceRate")
    void extractor_shouldCallGetInsuranceRate(@Mock Rate expectedRate) {
        // Arrange
        when(mockProvider.getInsuranceRate()).thenReturn(expectedRate);
        Function<TradeLoanParameterProvider, Object> extractor =
                TradeLoanFacilityFormulaField.INSURANCE_RATE.getExtractor();

        // Act
        Object result = extractor.apply(mockProvider);

        // Assert
        verify(mockProvider).getInsuranceRate();
        assertThat(result).isSameAs(expectedRate);
    }
}
