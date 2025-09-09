package ir.dotin.loan.trade.core.domain.loanfacility.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationResult;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationService;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@DisplayName("TradeInterestCalculationService Test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class TradeInterestCalculationServiceTest {

    @Mock
    private FormulaEvaluationService mockFormulaEvaluationService;

    @Mock
    private TradeLoanFacility mockLoanFacility;

    @Mock
    private LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>
            mockInterestFormula;

    @Mock
    private TradeLoanParameterProvider mockTradeLoanParameterProvider;

    @Mock
    private FormulaEvaluationResult mockFormulaEvaluationResult;

    private TradeInterestCalculationService service;

    @BeforeEach
    void setUp() {
        service = new TradeInterestCalculationService(mockFormulaEvaluationService);
    }

    @Nested
    @DisplayName("Constructor Tests")
    final class ConstructorTests {
        @Test
        @DisplayName("should create service with valid formula evaluation service")
        void shouldCreateServiceWithValidFormulaEvaluationService() {
            var newService = new TradeInterestCalculationService(mockFormulaEvaluationService);
            assertThat(newService).isNotNull();
        }

        @Test
        @DisplayName("should throw exception when formula evaluation service is null")
        void shouldThrowExceptionWhenFormulaEvaluationServiceIsNull() {
            assertThatThrownBy(() -> new TradeInterestCalculationService(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("formulaEvaluationService cannot be null");
        }
    }

    @Nested
    @DisplayName("Calculate Tests")
    final class CalculateTests {
        @Test
        @DisplayName("should calculate interest successfully")
        void shouldCalculateInterestSuccessfully(@Mock Money expectedMoney) {

            FormulaEvaluationResult formulaResult = new FormulaEvaluationResult(expectedMoney);
            Result<FormulaEvaluationResult> successfulEvalResult = Result.success(formulaResult);

            when(mockFormulaEvaluationService.evaluate(mockInterestFormula, mockTradeLoanParameterProvider))
                    .thenReturn(successfulEvalResult);

            // --- Act ---
            var result = service.calculate(mockLoanFacility, mockInterestFormula, mockTradeLoanParameterProvider);

            // --- Assert ---
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.value()).isEqualTo(expectedMoney);
            then(mockFormulaEvaluationService).should().evaluate(mockInterestFormula, mockTradeLoanParameterProvider);
        }

        @Test
        @DisplayName("should throw exception when facility is null")
        void shouldThrowExceptionWhenFacilityIsNull() {
            assertThatThrownBy(() -> service.calculate(null, mockInterestFormula, mockTradeLoanParameterProvider))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("facility cannot be null");
        }

        @Test
        @DisplayName("should throw exception when interest formula is null")
        void shouldThrowExceptionWhenInterestFormulaIsNull() {
            assertThatThrownBy(() -> service.calculate(mockLoanFacility, null, mockTradeLoanParameterProvider))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("interestFormula cannot be null");
        }

        @Test
        @DisplayName("should throw exception when trade loan parameter provider is null")
        void shouldThrowExceptionWhenTradeLoanParameterProviderIsNull() {
            assertThatThrownBy(() -> service.calculate(mockLoanFacility, mockInterestFormula, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("tradeLoanParameterProvider cannot be null");
        }

        @Test
        @DisplayName("should handle null loan application gracefully")
        void shouldHandleNullLoanApplicationGracefully() {

            given(mockFormulaEvaluationService.evaluate(mockInterestFormula, mockTradeLoanParameterProvider))
                    .willReturn(Result.success(mockFormulaEvaluationResult));

            // Act
            var result = service.calculate(mockLoanFacility, mockInterestFormula, mockTradeLoanParameterProvider);

            // Assert
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("Service Annotation Tests")
    final class ServiceAnnotationTests {
        @Test
        @DisplayName("should be annotated with DomainService")
        void shouldBeAnnotatedWithDomainService() {
            assertThat(TradeInterestCalculationService.class)
                    .hasAnnotation(ir.dotin.platform.domain.common.annotation.DomainService.class);
        }

        @Test
        @DisplayName("should be a final class")
        void shouldBeAFinalClass() {
            assertThat(TradeInterestCalculationService.class).isFinal();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    final class IntegrationTests {
        @Test
        @DisplayName("should delegate to formula evaluation service correctly")
        void shouldDelegateToFormulaEvaluationServiceCorrectly(@Mock Money expectedMoney) {

            given(mockFormulaEvaluationService.evaluate(mockInterestFormula, mockTradeLoanParameterProvider))
                    .willReturn(Result.success(mockFormulaEvaluationResult));
            given(mockFormulaEvaluationResult.value()).willReturn(expectedMoney);

            // Act
            var result = service.calculate(mockLoanFacility, mockInterestFormula, mockTradeLoanParameterProvider);

            // Assert
            assertThat(result.isSuccess()).isTrue();
            then(mockFormulaEvaluationService).should().evaluate(mockInterestFormula, mockTradeLoanParameterProvider);
        }
    }
}
