package ir.dotin.loan.trade.core.application.service.establishloanarrangement.conversion;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Formula;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPeriod;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.trade.core.application.ports.driven.command.EstablishTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.service.configuration.TradeLoanFormulaFieldMappingProperties;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Component
public class InstallmentPolicyDtoToInstallmentPolicyConverter
        implements Converter<
                EstablishTradeLoanArrangementCommand.InstallmentPolicyDto,
                Result<InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>> {

    private final TradeLoanFormulaFieldMappingProperties tradeLoanFieldMappingProperties;

    public InstallmentPolicyDtoToInstallmentPolicyConverter(
            TradeLoanFormulaFieldMappingProperties tradeLoanFieldMappingProperties) {
        this.tradeLoanFieldMappingProperties = tradeLoanFieldMappingProperties;
    }

    @Override
    public Result<InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>> convert(
            EstablishTradeLoanArrangementCommand.InstallmentPolicyDto source) {
        Result<InstallmentPeriod> periodResult = InstallmentPeriod.of(source.installmentPeriod());
        Result<Formula> installmentFormulaResult = Formula.valueOf(source.installmentFormula());
        Result<Formula> interestComponentFormulaResult = Formula.valueOf(source.interestComponentFormula());

        if (periodResult.isFailure()
                || installmentFormulaResult.isFailure()
                || interestComponentFormulaResult.isFailure()) {
            return Result.failure(periodResult
                    .notification()
                    .merge(installmentFormulaResult.notification())
                    .merge(interestComponentFormulaResult.notification()));
        }

        Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
                installmentParameterizedFormulaResult =
                        createParameterizedFormula(installmentFormulaResult.orElseThrow());
        Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
                interestComponentParameterizedFormulaResult =
                        createParameterizedFormula(interestComponentFormulaResult.orElseThrow());

        if (installmentParameterizedFormulaResult.isFailure()
                || interestComponentParameterizedFormulaResult.isFailure()) {
            return Result.failure(installmentParameterizedFormulaResult
                    .notification()
                    .merge(interestComponentParameterizedFormulaResult.notification()));
        }

        return InstallmentPolicy.of(
                periodResult.orElseThrow(),
                installmentParameterizedFormulaResult.orElseThrow(),
                interestComponentParameterizedFormulaResult.orElseThrow(),
                source.paymentType(),
                source.isDefineAutomaticInstallment());
    }

    private Result<LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>>
            createParameterizedFormula(Formula formula) {
        return LoanFacilityParameterizedFormula.valueOf(formula, tradeLoanFieldMappingProperties.fieldMap());
    }
}
