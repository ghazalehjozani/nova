package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;

import static java.util.Objects.requireNonNull;

public final class TradeSanctionedLoan extends AbstractSanctionedLoan<TradeSanctionedLoan.Builder> {

    private TradeSanctionedLoan(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    static Result<TradeSanctionedLoan> reconstitute(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.build();
    }

    @Override
    protected Result<Void> validateInternalState() {
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
