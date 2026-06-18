package ir.dotin.loan.trade.core.application.service.shared.formula;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.api.EvaluationOptions;
import ir.dotin.platform.formula.api.EvaluationResult;
import ir.dotin.platform.formula.api.Formula;
import ir.dotin.platform.formula.api.FormulaId;
import ir.dotin.platform.formula.api.ProviderRegistry;
import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.formula.api.binding.ParameterBinding;
import ir.dotin.platform.formula.api.spi.FormulaRegistry;
import ir.dotin.platform.formula.core.FormulaService;
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

    private final FormulaService formulaService;
    private final FormulaRegistry formulaRegistry;
    private final EvaluateFormulaViaFcbPort evaluateFormulaViaFcbPort;
    private final FormulaCorridorProperties formulaCorridorProperties;
    private final MeterRegistry meterRegistry;

    public TradeLoanFormulaEvaluationService(
            FormulaService formulaService,
            FormulaRegistry formulaRegistry,
            EvaluateFormulaViaFcbPort evaluateFormulaViaFcbPort,
            FormulaCorridorProperties formulaCorridorProperties,
            MeterRegistry meterRegistry) {
        this.formulaService = formulaService;
        this.formulaRegistry = formulaRegistry;
        this.evaluateFormulaViaFcbPort = evaluateFormulaViaFcbPort;
        this.formulaCorridorProperties = formulaCorridorProperties;
        this.meterRegistry = meterRegistry;
    }

    public BigDecimal evaluate(FormulaId formulaId, TradeLoanFacility facility) {
        return evaluate(formulaId, ProviderRegistry.of(TradeLoanParameterProvider.class, facility));
    }

    public BigDecimal evaluate(FormulaId formulaId, TradeLoanParameterProvider provider) {
        return evaluate(formulaId, ProviderRegistry.of(TradeLoanParameterProvider.class, provider));
    }

    public BigDecimal evaluate(FormulaId formulaId, ProviderRegistry providers) {
        Formula formula = formulaRegistry
                .get(formulaId)
                .orElseThrow(() -> new IllegalArgumentException("Formula not found: " + formulaId));

        BigDecimal primary = formulaService.evaluate(formula, providers, EvaluationOptions.financial());

        if (formulaCorridorProperties.isCrossCheckEnabled()) {
            crossCheckAgainstFcb(formula, providers, primary);
        }

        return primary;
    }

    public EvaluationResult evaluateWithDetails(FormulaId formulaId, TradeLoanFacility facility) {
        Formula formula = formulaRegistry.get(formulaId).orElseThrow();
        ProviderRegistry providers = ProviderRegistry.of(TradeLoanParameterProvider.class, facility);
        return formulaService.evaluateWithDetails(formula, providers);
    }

    public boolean isCompatible(FormulaId formulaId) {
        return formulaRegistry
                .get(formulaId)
                .map(f -> f.providerType()
                        .map(TradeLoanParameterProvider.class::isAssignableFrom)
                        .orElse(true))
                .orElse(false);
    }

    private void crossCheckAgainstFcb(Formula formula, ProviderRegistry providers, BigDecimal primary) {
        String code = formula.code().value();
        try {
            Map<String, BigDecimal> friendlyVars = extractFriendlyVars(formula, providers);
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

    private Map<String, BigDecimal> extractFriendlyVars(Formula formula, ProviderRegistry providers) {
        Map<String, BigDecimal> friendlyVars = new LinkedHashMap<>();
        Class<?> providerType = formula.providerType().orElse(null);
        Object provider = providerType == null
                ? null
                : providers.findCompatible(providerType).orElse(null);

        for (Map.Entry<String, ParameterBinding<?>> entry : formula.bindings().entrySet()) {
            if (!(entry.getValue() instanceof FieldBinding<?, ?> field) || provider == null) {
                continue;
            }
            BigDecimal value = toBigDecimal(extractValue(field, provider));
            if (value != null) {
                friendlyVars.put(entry.getKey(), value);
            }
        }
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
