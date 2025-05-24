package ir.dotin.loan.trade.core.domain.disbursement.formula;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

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
import ir.dotin.loan.trade.core.domain.loanfacility.aggregate.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.i18n.TradeLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFormulaContextProvider;

@DomainService
public final class TradeInterestCalculationService {

    private final FormulaContextProvider<BaseFormulaField, TradeLoanFacility> contextProvider;
    private final BaseFormulaFieldEvaluator formulaEvaluator;

    public TradeInterestCalculationService(
            TradeLoanFormulaContextProvider contextProvider, BaseFormulaFieldEvaluator formulaEvaluator) {
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider cannot be null");
        this.formulaEvaluator = Objects.requireNonNull(formulaEvaluator, "FormulaEvaluator cannot be null");
    }

    public Result<Money> calculateTotalInterest(InterestPolicy<BaseFormulaField> policy, TradeLoanFacility facility) {
        Objects.requireNonNull(policy, "InterestPolicy cannot be null");
        Objects.requireNonNull(facility, "facility cannot be null");

        ParameterizedFormula<BaseFormulaField> interestFormula = policy.interestFormula();
        if (interestFormula == null) {
            return Result.failure(
                    Notification.ofError(TradeLoanFacilityLocalizedMessageCodes.INTEREST_FORMULA_MISSING));
        }

        Result<Map<Character, TypedValue>> context = contextProvider.createContext(interestFormula, facility);

        if (context.isFailure()) {
            return Result.failure(context.notification());
        }

        Result<BigDecimal> calculationResult = formulaEvaluator.evaluate(interestFormula, context.value());

        if (calculationResult.isFailure()) {
            return Result.failure(calculationResult.notification());
        }

        return Money.valueOf(
                calculationResult.value(), facility.getLoanApplication().getCurrency());
    }
}
