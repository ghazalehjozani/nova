package ir.dotin.loan.trade.core.domain.disbursement.formula;

import java.math.BigDecimal;
import java.util.Map;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.platform.domain.common.vo.Money;
import ir.dotin.platform.domain.common.vo.ParameterizedFormula;
import ir.dotin.platform.domain.common.vo.TypedValue;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.formula.BaseFormulaField;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaContextProvider;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.BaseFormulaFieldEvaluator;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFormulaContextProvider;

import static java.util.Objects.requireNonNull;

@DomainService
public final class TradeInterestCalculationService {

    private final FormulaContextProvider<BaseFormulaField, TradeLoanFacility> contextProvider;
    private final BaseFormulaFieldEvaluator formulaEvaluator;

    public TradeInterestCalculationService(
            TradeLoanFormulaContextProvider contextProvider, BaseFormulaFieldEvaluator formulaEvaluator) {
        this.contextProvider = requireNonNull(contextProvider, "contextProvider cannot be null");
        this.formulaEvaluator = requireNonNull(formulaEvaluator, "FormulaEvaluator cannot be null");
    }

    public Result<Money> calculateTotalInterest(InterestPolicy<BaseFormulaField> policy, TradeLoanFacility facility) {
        requireNonNull(policy, "InterestPolicy cannot be null");
        requireNonNull(facility, "facility cannot be null");

        ParameterizedFormula<BaseFormulaField> interestFormula = policy.interestFormula();
        if (interestFormula == null) {
            return Result.failure(
                    Notification.ofError(TradeLoanFacilityLocalizedMessageCodes.INTEREST_FORMULA_MISSING));
        }

        Result<Map<Character, TypedValue>> context = contextProvider.createContext(interestFormula, facility);

        if (context.isFailure()) {
            return Result.failure(context.notification());
        }

        @SuppressWarnings("nullness")
        Map<Character, TypedValue> contextValue = context.value();
        Result<BigDecimal> calculationResult = formulaEvaluator.evaluate(interestFormula, contextValue);

        if (calculationResult.isFailure()) {
            return Result.failure(calculationResult.notification());
        }

        @SuppressWarnings("nullness")
        BigDecimal calculationValue = calculationResult.value();
        return Money.valueOf(calculationValue, facility.getLoanApplication().getCurrency());
    }
}
