package ir.dotin.loan.trade.core.domain.loanfacility.aggregate;

import java.util.Objects;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.aggregate.AbstractSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

public final class TradeSanctionedLoan
        extends AbstractSanctionedLoan<TradeSanctionedLoanId, TradeSanctionedLoan.Builder> {

    private TradeSanctionedLoan(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static Result<TradeSanctionedLoan> reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.build();
    }

    public static final class Builder
            extends AbstractSanctionedLoan.AbstractBuilder<TradeSanctionedLoanId, TradeSanctionedLoan, Builder> {

        @Override
        protected TradeSanctionedLoan buildInternal() {
            return new TradeSanctionedLoan(this);
        }

        @Override
        protected void validateSpecificRules(Notification notification) {}
    }
}
