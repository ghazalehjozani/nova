package ir.dotin.loan.trade.core.application.service.shared.formula;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.api.EvaluationOptions;
import ir.dotin.platform.formula.api.EvaluationResult;
import ir.dotin.platform.formula.api.Formula;
import ir.dotin.platform.formula.api.FormulaId;
import ir.dotin.platform.formula.api.ProviderRegistry;
import ir.dotin.platform.formula.api.spi.FormulaRegistry;
import ir.dotin.platform.formula.core.FormulaService;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Service
public class TradeLoanFormulaEvaluationService {

    private final FormulaService formulaService;
    private final FormulaRegistry formulaRegistry;

    public TradeLoanFormulaEvaluationService(FormulaService formulaService, FormulaRegistry formulaRegistry) {
        this.formulaService = formulaService;
        this.formulaRegistry = formulaRegistry;
    }

    /**
     * Evaluates a formula using the facility as parameter provider. The facility implements TradeLoanParameterProvider,
     * so it can be used directly.
     */
    public BigDecimal evaluate(FormulaId formulaId, TradeLoanFacility facility) {
        Formula formula = formulaRegistry
                .get(formulaId)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found: " + formulaId));

        // TradeLoanFacility IS the parameter provider!
        return formulaService.evaluate(formula, facility, EvaluationOptions.financial());
    }

    /** Evaluates with any TradeLoanParameterProvider. */
    public BigDecimal evaluate(FormulaId formulaId, TradeLoanParameterProvider provider) {
        Formula formula = formulaRegistry
                .get(formulaId)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found: " + formulaId));

        return formulaService.evaluate(formula, provider, EvaluationOptions.financial());
    }

    /** Evaluates with detailed result including timing and cache info. */
    public EvaluationResult evaluateWithDetails(FormulaId formulaId, TradeLoanFacility facility) {
        Formula formula = formulaRegistry.get(formulaId).orElseThrow();
        ProviderRegistry providers = ProviderRegistry.of(TradeLoanParameterProvider.class, facility);
        return formulaService.evaluateWithDetails(formula, providers);
    }

    /** Validates that a formula can be evaluated with TradeLoanParameterProvider. */
    public boolean isCompatible(FormulaId formulaId) {
        return formulaRegistry
                .get(formulaId)
                .map(f -> f.providerType()
                        .map(TradeLoanParameterProvider.class::isAssignableFrom)
                        .orElse(true)) // No provider required = compatible
                .orElse(false);
    }
}
