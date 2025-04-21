package ir.dotin.loan.morabehe.core.domain.loanfacility.service.validator;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.LoanFacilityCreationValidator;
import ir.dotin.loan.baseloan.core.domain.loanfacility.specification.*;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.aggregate.MorabeheLoanArrangement;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;
import ir.dotin.loan.morabehe.core.domain.loantype.aggregate.MorabeheLoanType;

@DomainService
public final class MorabeheLoanFacilityValidationService
        implements LoanFacilityCreationValidator<MorabeheLoanFacility, MorabeheLoanArrangement, MorabeheLoanType> {

    @Override
    public Result<Boolean> validateForCreation(
            MorabeheLoanFacility candidateFacility,
            MorabeheLoanArrangement morabeheRules,
            MorabeheLoanType morabeheLoanType) {

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
