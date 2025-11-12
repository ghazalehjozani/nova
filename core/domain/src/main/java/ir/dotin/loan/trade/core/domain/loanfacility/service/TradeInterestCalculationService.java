package ir.dotin.loan.trade.core.domain.loanfacility.service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.AbstractInterestCalculationService;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationResult;
import ir.dotin.loan.baseloan.core.domain.shared.formula.FormulaEvaluationService;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

import static java.util.Objects.requireNonNull;

@DomainService
public final class TradeInterestCalculationService
        extends AbstractInterestCalculationService<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> {

    private final FormulaEvaluationService formulaEvaluationService;

    public TradeInterestCalculationService(FormulaEvaluationService formulaEvaluationService) {
        super(formulaEvaluationService);
        this.formulaEvaluationService =
                requireNonNull(formulaEvaluationService, "formulaEvaluationService cannot be null.");
    }

    @Deprecated(forRemoval = true)
    public Result<Money> calculate(
            TradeLoanFacility facility,
            LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> interestFormula,
            TradeLoanParameterProvider tradeLoanParameterProvider) {

        requireNonNull(facility, "facility cannot be null.");
        requireNonNull(interestFormula, "interestFormula cannot be null.");
        requireNonNull(tradeLoanParameterProvider, "tradeLoanParameterProvider cannot be null.");

        Result<FormulaEvaluationResult> evaluationResult =
                formulaEvaluationService.evaluate(interestFormula, tradeLoanParameterProvider);

        return evaluationResult.map(formulaEvaluationResult -> Money.valueOf(
                        formulaEvaluationResult.value(),
                        requireNonNull(
                                facility.getSanctionedLoan().orElseThrow().getCurrency()))
                .orElseThrow());
    }
}
