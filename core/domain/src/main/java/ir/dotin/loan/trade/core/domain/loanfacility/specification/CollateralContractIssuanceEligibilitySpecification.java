package ir.dotin.loan.trade.core.domain.loanfacility.specification;

import java.util.List;
import java.util.Objects;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.NotificationError;
import ir.dotin.platform.commons.core.Verdict;
import ir.dotin.platform.commons.domain.validation.Specification;
import ir.dotin.loan.baseloan.core.domain.loanfacility.error.LoanFacilityErrors;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public final class CollateralContractIssuanceEligibilitySpecification implements Specification<TradeLoanFacility> {

    private final TradeLoanArrangement arrangement;

    public CollateralContractIssuanceEligibilitySpecification(TradeLoanArrangement arrangement) {
        this.arrangement = Objects.requireNonNull(arrangement, "arrangement cannot be null");
    }

    @Override
    public Verdict isSatisfiedBy(TradeLoanFacility facility) {
        Objects.requireNonNull(facility, "facility cannot be null");

        if (arrangement.getCollateralPolicy().totalPercent() > 0) {
            List<Collateral> facilityCollaterals = facility.getCollaterals();

            if (facilityCollaterals == null || facilityCollaterals.isEmpty()) {
                NotificationError error = NotificationError.of(LoanFacilityErrors.COLLATERAL_REQUIRED);
                return Verdict.notSatisfied(Notification.ofError(error));
            }
        }

        return Verdict.satisfied();
    }
}
