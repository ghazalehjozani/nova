package ir.dotin.loan.trade.core.domain.loanfacility.specification;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.NotificationError;
import ir.dotin.platform.commons.core.Verdict;
import ir.dotin.platform.commons.domain.validation.Specification;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanFacility;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.error.LoanFacilityErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import static java.util.Objects.requireNonNull;

public final class DisbursementMethodCompatibilitySpecification
        implements Specification<AbstractLoanFacility<?, ?, ?>> {

    private final TradeLoanArrangement tradeLoanArrangement;

    public DisbursementMethodCompatibilitySpecification(TradeLoanArrangement tradeLoanArrangement) {
        this.tradeLoanArrangement = requireNonNull(tradeLoanArrangement);
    }

    @Override
    public Verdict isSatisfiedBy(AbstractLoanFacility<?, ?, ?> facility) {
        requireNonNull(facility, "facility cannot be null");
        DisbursementType arrangementDisbursementType = tradeLoanArrangement.getDisbursementType();
        DisbursementMethod applicationDisbursementMethod =
                facility.getLoanApplication().getDisbursementMethod();
        if (!arrangementDisbursementType.allows(applicationDisbursementMethod)) {
            NotificationError error = NotificationError.of(
                    LoanFacilityErrors.DISBURSEMENT_METHOD_MISMATCH,
                    applicationDisbursementMethod,
                    arrangementDisbursementType);
            return Verdict.notSatisfied(Notification.ofError(error));
        }
        return Verdict.satisfied();
    }
}
