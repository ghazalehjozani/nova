package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;

public final class TradeSanctionedLoan extends AbstractSanctionedLoan<TradeSanctionedLoan.Builder> {

    private TradeSanctionedLoan(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Result<Unit> validateInternalState() {
        return super.validateInternalState();
    }

    public static final class Builder extends AbstractSanctionedLoanBuilder<TradeSanctionedLoan, Builder> {

        @Override
        public TradeSanctionedLoan buildInternal() {
            return new TradeSanctionedLoan(this);
        }

        @Override
        protected void validateSpecificRules(Notification notification) {}
    }
}
