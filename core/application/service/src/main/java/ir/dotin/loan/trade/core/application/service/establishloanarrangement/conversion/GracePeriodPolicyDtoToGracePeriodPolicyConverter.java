package ir.dotin.loan.trade.core.application.service.establishloanarrangement.conversion;

import java.time.Period;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Formula;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.GracePeriodPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.configuration.TradeLoanFormulaFieldMappingProperties;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class GracePeriodPolicyDtoToGracePeriodPolicyConverter
        implements Converter<
                EstablishTradeLoanArrangementCommand.GracePeriodPolicyDto,
                Result<GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>> {

    private final TradeLoanFormulaFieldMappingProperties tradeLoanFieldMappingProperties;

    public GracePeriodPolicyDtoToGracePeriodPolicyConverter(
            TradeLoanFormulaFieldMappingProperties tradeLoanFieldMappingProperties) {
        this.tradeLoanFieldMappingProperties = tradeLoanFieldMappingProperties;
    }

    @Override
    public Result<GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> convert(
            EstablishTradeLoanArrangementCommand.GracePeriodPolicyDto source) {
        Period minPeriod = Period.ofDays(source.minGracePeriodDays());
        Period maxPeriod = Period.ofDays(source.maxGracePeriodDays());
        Result<Formula> formulaResult = Formula.valueOf(source.formula());

        if (formulaResult.isFailure()) {
            return Result.failure(formulaResult.notification());
        }

        Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
                gracePeriodFormulaResult = createParameterizedFormula(formulaResult.orElseThrow());

        if (gracePeriodFormulaResult.isFailure()) {
            return Result.failure(gracePeriodFormulaResult.notification());
        }

        return GracePeriodPolicy.of(minPeriod, maxPeriod, gracePeriodFormulaResult.orElseThrow());
    }

    private Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
            createParameterizedFormula(Formula formula) {
        return LoanFacilityParameterizedFormula.valueOf(formula, tradeLoanFieldMappingProperties.fieldMap());
    }
}
