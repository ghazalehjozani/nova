package ir.dotin.loan.trade.core.domain.loanfacility.entity;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractLoanApplication;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;

import static java.util.Objects.requireNonNull;

public final class TradeLoanApplication extends AbstractLoanApplication<TradeLoanApplication.Builder> {

    private TradeLoanApplication(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Result<TradeLoanApplication> create(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for create");
        return Result.success(builder.id(LoanApplicationId.generate()).build());
    }

    public static Result<TradeLoanApplication> reconstitute(Builder builder) {
        requireNonNull(builder, "Builder cannot be null for reconstitution");
        return Result.success(builder.build());
    }

    @Override
    protected void validateInternalState() {
        super.validateInternalState();
    }

    public static final class Builder extends AbstractLoanApplicationBuilder<TradeLoanApplication, Builder> {

        @Override
        public TradeLoanApplication buildInternal() {
            return new TradeLoanApplication(this);
        }

        @Override
        protected void validateSpecificRules(@NonNull Notification notification) {}
    }
}
