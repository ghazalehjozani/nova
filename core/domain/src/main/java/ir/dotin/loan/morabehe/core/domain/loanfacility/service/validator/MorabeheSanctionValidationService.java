package ir.dotin.loan.morabehe.core.domain.loanfacility.service.validator;

import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.SanctionValidationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.specification.*;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Sanction;
import ir.dotin.loan.morabehe.core.domain.loanarrangement.aggregate.MorabeheLoanArrangement;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;

@DomainService
public final class MorabeheSanctionValidationService
        implements SanctionValidationService<MorabeheLoanFacility, MorabeheLoanArrangement, Sanction, Collateral> {

    @Override
    public Result<Boolean> validateSanction(
            MorabeheLoanFacility loanFacility, MorabeheLoanArrangement loanArrangement, Sanction sanction) {

        return new SanctionAmountSpecification<>(loanFacility)
                .and(new SanctionDurationSpecification<>(loanFacility))
                .and(new SanctionGracePeriodSpecification<>(loanArrangement))
                .and(new SanctionPreferentialRateSpecification<>(loanArrangement))
                .isSatisfiedBy(sanction);
    }

    @Override
    public Result<Boolean> validateSanctionCollateral(
            MorabeheLoanArrangement loanArrangement, Collateral collateral, Sanction sanction) {

        return new SanctionCollateralTypeSpecification<>(loanArrangement)
                .and(new SanctionCollateralPercentSpecification<>(loanArrangement))
                .and(new SanctionCollateralExistSpecification(collateral.collateralSerial()))
                .isSatisfiedBy(sanction);
    }
}
