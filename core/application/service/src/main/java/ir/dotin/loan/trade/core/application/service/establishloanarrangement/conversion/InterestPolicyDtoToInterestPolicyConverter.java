package ir.dotin.loan.trade.core.application.service.establishloanarrangement.conversion;

import com.google.common.collect.Range;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Formula;
import ir.dotin.platform.commons.domain.vo.Rate;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.configuration.TradeLoanFormulaFieldMappingProperties;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class InterestPolicyDtoToInterestPolicyConverter
        implements Converter<
                EstablishTradeLoanArrangementCommand.InterestPolicyDto,
                Result<InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>> {

    private final TradeLoanFormulaFieldMappingProperties tradeLoanFieldMappingProperties;

    public InterestPolicyDtoToInterestPolicyConverter(
            TradeLoanFormulaFieldMappingProperties tradeLoanFieldMappingProperties) {
        this.tradeLoanFieldMappingProperties = tradeLoanFieldMappingProperties;
    }

    @Override
    public Result<InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> convert(
            EstablishTradeLoanArrangementCommand.InterestPolicyDto source) {
        Result<Rate> minRateResult = Rate.valueOf(source.minRate());
        Result<Rate> maxRateResult = Rate.valueOf(source.maxRate());
        Result<Formula> interestFormulaResult = Formula.valueOf(source.interestFormula());
        Result<Formula> refundFormulaResult = Formula.valueOf(source.refundFormula());

        if (minRateResult.isFailure()
                || maxRateResult.isFailure()
                || interestFormulaResult.isFailure()
                || refundFormulaResult.isFailure()) {
            var notification = minRateResult
                    .notification()
                    .merge(maxRateResult.notification())
                    .merge(interestFormulaResult.notification())
                    .merge(refundFormulaResult.notification());
            return Result.failure(notification);
        }

        Range<Rate> preferentialRangeRate = Range.closed(minRateResult.orElseThrow(), maxRateResult.orElseThrow());

        Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
                interestParameterizedFormulaResult = createParameterizedFormula(interestFormulaResult.orElseThrow());
        Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
                refundParameterizedFormulaResult = createParameterizedFormula(refundFormulaResult.orElseThrow());

        if (interestParameterizedFormulaResult.isFailure() || refundParameterizedFormulaResult.isFailure()) {
            return Result.failure(interestParameterizedFormulaResult
                    .notification()
                    .merge(refundParameterizedFormulaResult.notification()));
        }

        return InterestPolicy.of(
                minRateResult.value(), // baseInterestRate
                preferentialRangeRate,
                interestParameterizedFormulaResult.orElseThrow(),
                refundParameterizedFormulaResult.orElseThrow(),
                source.dailyInterest());
    }

    private Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
            createParameterizedFormula(Formula formula) {
        return LoanFacilityParameterizedFormula.valueOf(formula, tradeLoanFieldMappingProperties.fieldMap());
    }
}
