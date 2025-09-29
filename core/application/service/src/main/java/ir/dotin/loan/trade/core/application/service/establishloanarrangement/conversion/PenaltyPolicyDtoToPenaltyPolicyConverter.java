package ir.dotin.loan.trade.core.application.service.establishloanarrangement.conversion;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Formula;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.PenaltyPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.configuration.TradeLoanFormulaFieldMappingProperties;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class PenaltyPolicyDtoToPenaltyPolicyConverter
        implements Converter<
                EstablishTradeLoanArrangementCommand.PenaltyPolicyDto,
                Result<PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>> {

    private final TradeLoanFormulaFieldMappingProperties tradeLoanFieldMappingProperties;

    public PenaltyPolicyDtoToPenaltyPolicyConverter(
            TradeLoanFormulaFieldMappingProperties tradeLoanFieldMappingProperties) {
        this.tradeLoanFieldMappingProperties = tradeLoanFieldMappingProperties;
    }

    @Override
    public Result<PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> convert(
            EstablishTradeLoanArrangementCommand.PenaltyPolicyDto source) {
        Result<Rate> penaltyRateResult = Rate.valueOf(source.penaltyRate());
        Result<Rate> deferralRateResult = Rate.valueOf(source.deferralInterestRate());
        Result<Formula> formulaResult = Formula.valueOf(source.formula());

        if (penaltyRateResult.isFailure() || deferralRateResult.isFailure() || formulaResult.isFailure()) {
            return Result.failure(penaltyRateResult
                    .notification()
                    .merge(deferralRateResult.notification())
                    .merge(formulaResult.notification()));
        }

        Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
                penaltyFormulaResult = createParameterizedFormula(formulaResult.orElseThrow());

        if (penaltyFormulaResult.isFailure()) {
            return Result.failure(penaltyFormulaResult.notification());
        }

        return PenaltyPolicy.of(
                penaltyRateResult.orElseThrow(),
                deferralRateResult.orElseThrow(),
                penaltyFormulaResult.orElseThrow(),
                source.paymentType());
    }

    private Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
            createParameterizedFormula(Formula formula) {
        return LoanFacilityParameterizedFormula.valueOf(formula, tradeLoanFieldMappingProperties.fieldMap());
    }
}
