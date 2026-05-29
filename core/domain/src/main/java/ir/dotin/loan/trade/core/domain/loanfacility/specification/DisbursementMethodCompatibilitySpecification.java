package ir.dotin.loan.trade.core.domain.loanfacility.specification;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.NotificationError;
import ir.dotin.platform.pangaea.commons.core.Verdict;
import ir.dotin.platform.pangaea.commons.domain.validation.Specification;
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
        @org.jspecify.annotations.Nullable
        DisbursementMethod applicationDisbursementMethod =
                facility.getLoanApplication().getDisbursementMethod();
        if (applicationDisbursementMethod == null
                || !arrangementDisbursementType.allows(applicationDisbursementMethod)) {
            // Pass "null" string representation when disbursement method is absent so that the
            // @NonNull vararg slot in NotificationError.of() is satisfied.
            NotificationError error = NotificationError.of(
                    LoanFacilityErrors.DISBURSEMENT_METHOD_MISMATCH,
                    String.valueOf(applicationDisbursementMethod),
                    arrangementDisbursementType);
            return Verdict.notSatisfied(Notification.ofError(error));
        }
        return Verdict.satisfied();
    }
}
