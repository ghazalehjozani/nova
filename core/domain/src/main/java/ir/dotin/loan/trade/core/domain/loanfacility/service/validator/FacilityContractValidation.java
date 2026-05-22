package ir.dotin.loan.trade.core.domain.loanfacility.service.validator;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Verdict;
import ir.dotin.platform.pangaea.commons.domain.annotation.DomainService;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.specification.CollateralContractIssuanceEligibilitySpecification;

@DomainService
public final class FacilityContractValidation {

    public Result<Boolean> validateForContractIssuance(TradeLoanFacility facility, TradeLoanArrangement arrangement) {

        var contractIssuanceSpecification = new CollateralContractIssuanceEligibilitySpecification(arrangement);
        Verdict verdict = contractIssuanceSpecification.isSatisfiedBy(facility);
        return verdict.isSatisfied() ? Result.success(true) : Result.failure(verdict.reasons());
    }
}
