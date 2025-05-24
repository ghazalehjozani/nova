package ir.dotin.loan.trade.core.domain.loanfacility.service.validator;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.LoanFacilityCreationValidator;
import ir.dotin.loan.baseloan.core.domain.loanfacility.specification.*;
import ir.dotin.loan.trade.core.domain.loanarrangement.aggregate.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.aggregate.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.aggregate.TradeLoanType;

@DomainService
public final class TradeLoanFacilityValidationService
        implements LoanFacilityCreationValidator<TradeLoanFacility, TradeLoanArrangement, TradeLoanType> {

    @Override
    public Result<Boolean> validateForCreation(
            TradeLoanFacility candidateFacility, TradeLoanArrangement morabeheRules, TradeLoanType morabeheLoanType) {

        return new LoanApplicationAmountSpecification<>(morabeheRules.getAmountRange())
                .and(new LoanApplicationDurationSpecification<>(morabeheRules.getDurationRange()))
                .and(new LoanApplicationGracePeriodSpecification<>(
                        morabeheRules.getGracePeriodPolicy().gracePeriodRange()))
                .and(new LoanApplicationGuarantorCountSpecification<>(morabeheRules.getGuarantorCount()))
                .and(new LoanApplicationCustomerTypeSpecification<>(morabeheRules.getPartyType()))
                .and(new LoanApplicationInstallmentCountSpecification<>(
                        morabeheRules.getInstallmentPolicy().installmentPaymentType()))
                .and(new LoanApplicationEconomicSectorSpecification<>(morabeheLoanType.getEconomicSectors()))
                .and(new LoanApplicationCurrencySpecification<>(morabeheRules.getCurrencies()))
                .isSatisfiedBy(candidateFacility);
    }
}
