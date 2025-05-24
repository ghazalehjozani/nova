package ir.dotin.loan.trade.core.domain.loanfacility.aggregate;

import java.util.Objects;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.aggregate.AbstractLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.vo.TradeLoanApplicationId;

public final class TradeLoanApplication
        extends AbstractLoanApplication<TradeLoanApplicationId, TradeLoanApplication.Builder> {

    private TradeLoanApplication(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static Result<TradeLoanApplication> create(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for create.");
        return builder.withId(TradeLoanApplicationId.generate()).build();
    }

    static Result<TradeLoanApplication> reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.build();
    }

    public static final class Builder
            extends AbstractLoanApplication.AbstractBuilder<TradeLoanApplicationId, TradeLoanApplication, Builder> {

        @Override
        protected TradeLoanApplication buildInternal() {
            return new TradeLoanApplication(this);
        }

        @Override
        protected void validateSpecificRules(Notification notification) {}
    }
}
