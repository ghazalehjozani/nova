package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanFacility;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.LoanFacilityEventFactory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.core.domain.loanarrangement.vo.TradeLoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityEvent;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanFacilityId;
import ir.dotin.loan.trade.core.domain.loantype.vo.TradeLoanTypeId;

import static java.util.Objects.requireNonNull;

public final class TradeLoanFacility
        extends AbstractLoanFacility<TradeLoanFacilityId, TradeLoanApplication, TradeSanctionedLoan> {

    private final TradeLoanTypeId tradeLoanTypeId;
    private final TradeLoanArrangementId tradeLoanArrangementId;

    TradeLoanFacility(
            TradeLoanFacilityId id,
            TradeLoanApplication application,
            @Nullable TradeSanctionedLoan sanctionedLoan,
            FacilityStatus status,
            TradeLoanTypeId loanTypeId,
            TradeLoanArrangementId loanArrangementId) {
        super(id, application, sanctionedLoan, status, loanArrangementId);
        this.tradeLoanTypeId = requireNonNull(loanTypeId, "tradeLoanTypeId cannot be null");
        this.tradeLoanArrangementId = requireNonNull(loanArrangementId, "tradeLoanArrangementId cannot be null");
    }

    @Override
    public TradeLoanTypeId getLoanTypeId() {
        return tradeLoanTypeId;
    }

    @Override
    public TradeLoanArrangementId getLoanArrangementId() {
        return tradeLoanArrangementId;
    }

    @Override
    public String getLoanFacilityType() {
        return "TRADE";
    }

    @Override
    protected LoanFacilityEventFactory<TradeLoanFacilityEvent<?, ?>> createEventFactory() {
        return new TradeLoanFacilityEventFactory();
    }
}
