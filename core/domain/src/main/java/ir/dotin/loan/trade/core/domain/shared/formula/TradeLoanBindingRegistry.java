package ir.dotin.loan.trade.core.domain.shared.formula;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ir.dotin.platform.formula.api.binding.FieldBinding;
import ir.dotin.platform.formula.api.binding.ParameterBinding;

/**
 * Registry of all available bindings for Trade Loan formulas. Used for:
 *
 * <ol>
 *   <li>Validate formula variables at creation time
 *   <li>Reconstruct Formula from FormulaId + expression at runtime
 *   <li>Document available parameters for API consumers
 * </ol>
 */
public final class TradeLoanBindingRegistry {

    private static final Map<String, FieldBinding<TradeLoanParameterProvider, ?>> BINDINGS;

    static {
        Map<String, FieldBinding<TradeLoanParameterProvider, ?>> map = new LinkedHashMap<>();

        map.put("approvedAmount", TradeLoanBindings.TRADE_APPROVED_AMOUNT);
        map.put("requestedAmount", TradeLoanBindings.TRADE_REQUESTED_AMOUNT);
        map.put("durationMonths", TradeLoanBindings.TRADE_DURATION_MONTHS);
        map.put("installmentCount", TradeLoanBindings.TRADE_INSTALLMENT_COUNT);
        map.put("gracePeriodMonths", TradeLoanBindings.TRADE_GRACE_PERIOD);
        map.put("totalDisbursed", TradeLoanBindings.TRADE_TOTAL_DISBURSED);

        BINDINGS = Collections.unmodifiableMap(map);
    }

    private TradeLoanBindingRegistry() {}

    /** Returns all known variable names. */
    public static Set<String> knownVariables() {
        return BINDINGS.keySet();
    }

    /**
     * Resolves a binding by variable name.
     *
     * @throws IllegalArgumentException if variable unknown
     */
    public static FieldBinding<TradeLoanParameterProvider, ?> resolve(String variable) {
        FieldBinding<TradeLoanParameterProvider, ?> binding = BINDINGS.get(variable);
        if (binding == null) {
            throw new IllegalArgumentException(
                    "Unknown formula variable: '" + variable + "'. Known: " + BINDINGS.keySet());
        }
        return binding;
    }

    /** Resolves multiple bindings. */
    public static Map<String, ParameterBinding<?>> resolveAll(Set<String> variables) {
        Map<String, ParameterBinding<?>> result = new LinkedHashMap<>();
        for (String var : variables) {
            result.put(var, resolve(var));
        }
        return result;
    }

    /** Checks if all variables are known. */
    public static boolean areAllKnown(Set<String> variables) {
        return BINDINGS.keySet().containsAll(variables);
    }

    /** Returns unknown variables from the given set. */
    public static Set<String> findUnknown(Set<String> variables) {
        return variables.stream().filter(v -> !BINDINGS.containsKey(v)).collect(Collectors.toSet());
    }
}
