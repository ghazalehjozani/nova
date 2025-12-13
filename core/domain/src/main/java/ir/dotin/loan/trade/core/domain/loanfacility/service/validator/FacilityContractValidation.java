package ir.dotin.loan.trade.core.domain.loanfacility.service.validator;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.annotation.DomainService;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.specification.CollateralContractIssuanceEligibilitySpecification;

@DomainService
public final class FacilityContractValidation {

    public Result<Boolean> validateForContractIssuance(TradeLoanFacility facility, TradeLoanArrangement arrangement) {

        var contractIssuanceSpecification = new CollateralContractIssuanceEligibilitySpecification(arrangement);
        return contractIssuanceSpecification.isSatisfiedBy(facility);
    }
}
