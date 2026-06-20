package ir.dotin.loan.trade.core.application.service.shared.formula;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.api.EvaluationResult;
import ir.dotin.platform.formula.api.Formula;
import ir.dotin.platform.formula.api.FormulaId;
import ir.dotin.platform.formula.api.ProviderRegistry;
import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.formula.api.binding.ParameterBinding;
import ir.dotin.platform.formula.api.exception.EvaluationException;
import ir.dotin.platform.formula.api.exception.FormulaNotFoundException;
import ir.dotin.platform.formula.api.spi.FormulaRegistry;
import ir.dotin.platform.formula.service.FormulaEvaluationCoordinator;
import ir.dotin.platform.formula.service.FormulaEvaluationCoordinator.ResolvedFormula;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.trade.core.application.ports.outbound.client.formula.EvaluateFormulaViaFcbPort;
import ir.dotin.loan.trade.core.application.service.configuration.FormulaCorridorProperties;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TradeLoanFormulaEvaluationService {

    private static final BigDecimal CROSS_CHECK_EPSILON = new BigDecimal("0.0001");

    private final FormulaRegistry formulaRegistry;
    private final FormulaEvaluationCoordinator coordinator;
    private final EvaluateFormulaViaFcbPort evaluateFormulaViaFcbPort;
    private final FormulaCorridorProperties formulaCorridorProperties;
    private final MeterRegistry meterRegistry;

    public TradeLoanFormulaEvaluationService(
            FormulaRegistry formulaRegistry,
            FormulaEvaluationCoordinator coordinator,
            EvaluateFormulaViaFcbPort evaluateFormulaViaFcbPort,
            FormulaCorridorProperties formulaCorridorProperties,
            MeterRegistry meterRegistry) {
        this.formulaRegistry = formulaRegistry;
        this.coordinator = coordinator;
        this.evaluateFormulaViaFcbPort = evaluateFormulaViaFcbPort;
        this.formulaCorridorProperties = formulaCorridorProperties;
        this.meterRegistry = meterRegistry;
    }

    public BigDecimal evaluate(String code, Map<String, String> providerRefs, Map<String, BigDecimal> overrides) {
        ResolvedFormula resolved = coordinator.resolve(code, providerRefs);
        return runEngine(resolved.formula(), resolved.providers(), overrides);
    }

    public BigDecimal evaluate(FormulaId formulaId, TradeLoanFacility facility) {
        return evaluate(formulaId, ProviderRegistry.of(TradeLoanParameterProvider.class, facility));
    }

    public BigDecimal evaluate(FormulaId formulaId, ProviderRegistry providers) {
        Formula formula = formulaRegistry.get(formulaId).orElseThrow(() -> new FormulaNotFoundException(formulaId));
        return runEngine(formula, providers, Map.of());
    }

    public EvaluationResult evaluateWithDetails(FormulaId formulaId, TradeLoanFacility facility) {
        Formula formula = formulaRegistry.get(formulaId).orElseThrow(() -> new FormulaNotFoundException(formulaId));
        ProviderRegistry providers = ProviderRegistry.of(TradeLoanParameterProvider.class, facility);
        return coordinator.computeLocal(new ResolvedFormula(formula, providers), Map.of());
    }

    public boolean isCompatible(FormulaId formulaId) {
        return formulaRegistry
                .get(formulaId)
                .map(f -> f.providerTypes().isEmpty()
                        || f.providerTypes().stream().anyMatch(TradeLoanParameterProvider.class::isAssignableFrom))
                .orElse(false);
    }

    private BigDecimal runEngine(Formula formula, ProviderRegistry providers, Map<String, BigDecimal> overrides) {
        return switch (formulaCorridorProperties.getEngine()) {
            case FCB -> evaluateViaFcb(formula, providers, overrides);
            case EXPRESSION_KIT -> {
                BigDecimal primary = coordinator
                        .computeLocal(new ResolvedFormula(formula, providers), overrides)
                        .value();
                if (formulaCorridorProperties.isCrossCheckEnabled()) {
                    crossCheckAgainstFcb(formula, providers, overrides, primary);
                }
                yield primary;
            }
        };
    }

    private BigDecimal evaluateViaFcb(Formula formula, ProviderRegistry providers, Map<String, BigDecimal> overrides) {
        String code = formula.code().value();
        Map<String, BigDecimal> friendlyVars = friendlyVars(formula, providers, overrides);
        Result<BigDecimal> fcbResult = evaluateFormulaViaFcbPort.evaluateViaFcb(code, friendlyVars);
        if (fcbResult.isFailure()) {
            String cause = fcbResult.err().map(FailureCause::message).orElse("unknown");
            throw new EvaluationException("FCB formula evaluation failed for '" + code + "': " + cause, formula.code());
        }
        return fcbResult.unwrap();
    }

    private void crossCheckAgainstFcb(
            Formula formula, ProviderRegistry providers, Map<String, BigDecimal> overrides, BigDecimal primary) {
        String code = formula.code().value();
        try {
            Map<String, BigDecimal> friendlyVars = friendlyVars(formula, providers, overrides);
            Result<BigDecimal> fcbResult = evaluateFormulaViaFcbPort.evaluateViaFcb(code, friendlyVars);
            if (fcbResult.isFailure()) {
                log.warn(
                        "FCB formula cross-check failed for '{}' (primary={}, expression-kit authoritative): {}",
                        code,
                        primary,
                        fcbResult.err().map(FailureCause::message).orElse("unknown"));
                Counter.builder("formula.crosscheck.error")
                        .tag("code", code)
                        .register(meterRegistry)
                        .increment();
                return;
            }
            BigDecimal fcb = fcbResult.unwrap();
            if (diverges(primary, fcb)) {
                log.warn(
                        "FCB formula cross-check diverged for '{}': primary={}, fcb={} (expression-kit authoritative)",
                        code,
                        primary,
                        fcb);
                meterRegistry
                        .counter("formula.crosscheck.divergence", "code", code)
                        .increment();
            }
        } catch (RuntimeException ex) {
            log.warn("FCB formula cross-check transport error for '{}' (expression-kit authoritative)", code, ex);
            Counter.builder("formula.crosscheck.error")
                    .tag("code", code)
                    .register(meterRegistry)
                    .increment();
        }
    }

    private Map<String, BigDecimal> friendlyVars(
            Formula formula, ProviderRegistry providers, Map<String, BigDecimal> overrides) {
        Map<String, BigDecimal> friendlyVars = new LinkedHashMap<>();
        for (Map.Entry<String, ParameterBinding<?>> entry : formula.bindings().entrySet()) {
            String variable = entry.getKey();
            if (!(entry.getValue() instanceof FieldBinding<?, ?> field)) {
                continue;
            }
            Class<?> providerType = formula.bindingProviderTypes().get(variable);
            if (providerType == null) {
                continue;
            }
            Object provider = providers.findCompatible(providerType).orElse(null);
            if (provider == null) {
                continue;
            }
            BigDecimal value = toBigDecimal(extractValue(field, provider));
            if (value != null) {
                friendlyVars.put(variable, value);
            }
        }
        friendlyVars.putAll(overrides);
        return friendlyVars;
    }

    @SuppressWarnings("unchecked")
    private @Nullable Object extractValue(FieldBinding<?, ?> field, Object provider) {
        try {
            return ((FieldBinding<Object, ?>) field).extract(provider);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private @Nullable BigDecimal toBigDecimal(@Nullable Object value) {
        return switch (value) {
            case null -> null;
            case BigDecimal bd -> bd;
            case Money money -> money.value();
            case Number number -> new BigDecimal(number.toString());
            default -> null;
        };
    }

    private boolean diverges(BigDecimal primary, @Nullable BigDecimal fcb) {
        if (fcb == null) {
            return true;
        }
        return primary.subtract(fcb).abs().compareTo(CROSS_CHECK_EPSILON) > 0;
    }
}
