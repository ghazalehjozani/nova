package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeSanctionedLoanId;

import static java.util.Objects.requireNonNull;

public final class TradeSanctionedLoan
        extends AbstractSanctionedLoan<TradeSanctionedLoanId, TradeSanctionedLoan.TradeSanctionedLoanBuilder> {

    private TradeSanctionedLoan(TradeSanctionedLoanBuilder builder) {
        super(builder);
    }

    public static TradeSanctionedLoanBuilder newBuilder() {
        return new TradeSanctionedLoanBuilder();
    }

    static Result<TradeSanctionedLoan> reconstitute(TradeSanctionedLoanBuilder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.build();
    }

    @Override
    protected Result<Void> validateInternalState() {
        return super.validateInternalState();
    }

    public static final class TradeSanctionedLoanBuilder
            extends AbstractSanctionedLoanBuilder<
                    TradeSanctionedLoanId, TradeSanctionedLoan, TradeSanctionedLoanBuilder> {

        @Override
        protected TradeSanctionedLoan buildInternal() {
            return new TradeSanctionedLoan(this);
        }

        @Override
        protected void validateSpecificRules(Notification notification) {}
    }
}
