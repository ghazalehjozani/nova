package ir.dotin.loan.trade.core.domain.loanfacility.service.validator;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.Verdict;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.LoanFacilityCreationValidator;
import ir.dotin.loan.baseloan.core.domain.loanfacility.specification.*;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.specification.DisbursementMethodCompatibilitySpecification;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

@DomainService
public final class TradeLoanFacilityValidationService
        implements LoanFacilityCreationValidator<TradeLoanFacility, TradeLoanArrangement, TradeLoanType> {

    @Override
    public Result<Boolean> validateForCreation(
            TradeLoanFacility candidateFacility, TradeLoanArrangement tradeRules, TradeLoanType tradeLoanType) {

        var baseSpecification = new LoanApplicationAmountSpecification(tradeRules.getAmountRange())
                .and(new LoanApplicationDurationSpecification(tradeRules.getDurationRange()))
                .and(new LoanApplicationGracePeriodSpecification(
                        tradeRules.getGracePeriodPolicy().minGracePeriod(),
                        tradeRules.getGracePeriodPolicy().maxGracePeriod()))
                .and(new LoanApplicationCustomerTypeSpecification(tradeRules.getPartyType()));

        // Add guarantor count specification only if guarantor count is specified
        var specificationWithGuarantor = tradeRules.getGuarantorCount() != null
                ? baseSpecification.and(new LoanApplicationGuarantorCountSpecification(tradeRules.getGuarantorCount()))
                : baseSpecification;

        Verdict verdict = specificationWithGuarantor
                .and(new LoanApplicationInstallmentCountSpecification(
                        tradeRules.getInstallmentPolicy().installmentPaymentType()))
                .and(new DisbursementMethodCompatibilitySpecification(tradeRules))
                //                .and(new
                // LoanApplicationEconomicSectorSpecification(tradeLoanType.getEconomicSectorCurrencies())) TODO:
                //                .and(new LoanApplicationCurrencySpecification(tradeRules.getCurrencyType()))
                .isSatisfiedBy(candidateFacility);
        return verdict.isSatisfied() ? Result.success(true) : Result.failure(verdict.reasons());
    }
}
