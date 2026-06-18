package ir.dotin.loan.trade.core.application.service.shared.formula;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.formula.api.Formula;
import ir.dotin.platform.formula.api.FormulaExpression;
import ir.dotin.platform.formula.api.FormulaId;
import ir.dotin.platform.formula.api.ProviderRegistry;
import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.formula.api.spi.FormulaRegistry;
import ir.dotin.platform.formula.core.FormulaService;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.formula.EvaluateFormulaViaFcbPort;
import ir.dotin.loan.trade.core.application.service.configuration.FormulaCorridorProperties;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class TradeLoanFormulaEvaluationServiceTest {

    private static final FormulaId FORMULA_ID = FormulaId.of("interest");

    @Mock
    private FormulaService formulaService;

    @Mock
    private FormulaRegistry formulaRegistry;

    @Mock
    private EvaluateFormulaViaFcbPort evaluateFormulaViaFcbPort;

    @Mock
    private TradeLoanParameterProvider provider;

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final FormulaCorridorProperties properties = new FormulaCorridorProperties();

    private TradeLoanFormulaEvaluationService service;
    private Formula formula;
    private ProviderRegistry providers;

    @BeforeEach
    void setUp() {
        service = new TradeLoanFormulaEvaluationService(
                formulaService, formulaRegistry, evaluateFormulaViaFcbPort, properties, meterRegistry);

        formula = FormulaExpression.builder("approvedAmount * rate", TradeLoanParameterProvider.class)
                .id(FORMULA_ID)
                .bind("approvedAmount", FieldBinding.of("approvedAmount", p -> new BigDecimal("1000")))
                .build();
        providers = ProviderRegistry.of(TradeLoanParameterProvider.class, provider);

        when(formulaRegistry.get(FORMULA_ID)).thenReturn(Optional.of(formula));
    }

    @Test
    void flagOffSkipsFcbAndReturnsPrimary() {
        properties.setCrossCheckEnabled(false);
        when(formulaService.evaluate(eq(formula), any(ProviderRegistry.class), any()))
                .thenReturn(new BigDecimal("42"));

        BigDecimal result = service.evaluate(FORMULA_ID, providers);

        assertThat(result).isEqualByComparingTo("42");
        verify(evaluateFormulaViaFcbPort, never()).evaluateViaFcb(anyString(), anyMap());
    }

    @Test
    void crossCheckMatchReturnsPrimaryNoDivergenceMetric() {
        properties.setCrossCheckEnabled(true);
        when(formulaService.evaluate(eq(formula), any(ProviderRegistry.class), any()))
                .thenReturn(new BigDecimal("42"));
        when(evaluateFormulaViaFcbPort.evaluateViaFcb(eq("interest"), anyMap()))
                .thenReturn(Result.success(new BigDecimal("42.00000")));

        BigDecimal result = service.evaluate(FORMULA_ID, providers);

        assertThat(result).isEqualByComparingTo("42");
        assertThat(meterRegistry.find("formula.crosscheck.divergence").counters())
                .isEmpty();
    }

    @Test
    void crossCheckDivergenceLogsCountsButReturnsPrimary() {
        properties.setCrossCheckEnabled(true);
        when(formulaService.evaluate(eq(formula), any(ProviderRegistry.class), any()))
                .thenReturn(new BigDecimal("42"));
        when(evaluateFormulaViaFcbPort.evaluateViaFcb(eq("interest"), anyMap()))
                .thenReturn(Result.success(new BigDecimal("99")));

        BigDecimal result = service.evaluate(FORMULA_ID, providers);

        assertThat(result).isEqualByComparingTo("42");
        assertThat(meterRegistry.find("formula.crosscheck.divergence").counters())
                .isNotEmpty();
    }

    @Test
    void crossCheckFcbFailureCountsErrorButReturnsPrimary() {
        properties.setCrossCheckEnabled(true);
        when(formulaService.evaluate(eq(formula), any(ProviderRegistry.class), any()))
                .thenReturn(new BigDecimal("42"));
        when(evaluateFormulaViaFcbPort.evaluateViaFcb(eq("interest"), anyMap()))
                .thenReturn(Result.failure(CoreBankingErrors.FORMULA_EVALUATION_FAILED_IN_FCB, "interest"));

        BigDecimal result = service.evaluate(FORMULA_ID, providers);

        assertThat(result).isEqualByComparingTo("42");
        assertThat(meterRegistry.find("formula.crosscheck.error").counters()).isNotEmpty();
    }

    @Test
    void crossCheckTransportErrorCountsErrorButReturnsPrimary() {
        properties.setCrossCheckEnabled(true);
        when(formulaService.evaluate(eq(formula), any(ProviderRegistry.class), any()))
                .thenReturn(new BigDecimal("42"));
        when(evaluateFormulaViaFcbPort.evaluateViaFcb(eq("interest"), anyMap()))
                .thenThrow(new RuntimeException("transport down"));

        BigDecimal result = service.evaluate(FORMULA_ID, providers);

        assertThat(result).isEqualByComparingTo("42");
        assertThat(meterRegistry.find("formula.crosscheck.error").counters()).isNotEmpty();
    }

    @Test
    void crossCheckExtractsFriendlyVarsFromFieldBindings() {
        properties.setCrossCheckEnabled(true);
        when(formulaService.evaluate(eq(formula), any(ProviderRegistry.class), any()))
                .thenReturn(new BigDecimal("42"));
        when(evaluateFormulaViaFcbPort.evaluateViaFcb(eq("interest"), anyMap()))
                .thenReturn(Result.success(new BigDecimal("42")));

        service.evaluate(FORMULA_ID, providers);

        verify(evaluateFormulaViaFcbPort)
                .evaluateViaFcb(
                        eq("interest"),
                        argThat(map -> new BigDecimal("1000").compareTo(map.get("approvedAmount")) == 0));
    }
}
