package ir.dotin.loan.morabehe.core.domain.disbursement.formula;

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
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;
import ir.dotin.loan.morabehe.core.domain.loanfacility.i18n.MorabeheLoanFacilityLocalizedMessageCodes;
import ir.dotin.loan.morabehe.core.domain.shared.formula.MorabeheLoanFormulaContextProvider;

@DomainService
public final class MorabeheInterestCalculationService {

    private final FormulaContextProvider<BaseFormulaField, MorabeheLoanFacility> contextProvider;
    private final BaseFormulaFieldEvaluator formulaEvaluator;

    public MorabeheInterestCalculationService(
            MorabeheLoanFormulaContextProvider contextProvider, BaseFormulaFieldEvaluator formulaEvaluator) {
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider cannot be null");
        this.formulaEvaluator = Objects.requireNonNull(formulaEvaluator, "FormulaEvaluator cannot be null");
    }

    public Result<Money> calculateTotalInterest(
            InterestPolicy<BaseFormulaField> policy, MorabeheLoanFacility facility) {
        Objects.requireNonNull(policy, "InterestPolicy cannot be null");
        Objects.requireNonNull(facility, "facility cannot be null");

        ParameterizedFormula<BaseFormulaField> interestFormula = policy.interestFormula();
        if (interestFormula == null) {
            return Result.failure(
                    Notification.ofError(MorabeheLoanFacilityLocalizedMessageCodes.INTEREST_FORMULA_MISSING));
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
