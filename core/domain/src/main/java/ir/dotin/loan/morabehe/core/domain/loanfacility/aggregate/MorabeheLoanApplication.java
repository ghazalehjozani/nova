package ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate;

import java.util.Objects;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.aggregate.AbstractLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheLoanApplicationId;

public final class MorabeheLoanApplication
        extends AbstractLoanApplication<MorabeheLoanApplicationId, MorabeheLoanApplication.Builder> {

    private MorabeheLoanApplication(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static Result<MorabeheLoanApplication> create(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for create.");
        return builder.withId(MorabeheLoanApplicationId.generate()).build();
    }

    static Result<MorabeheLoanApplication> reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.build();
    }

    public static final class Builder
            extends AbstractLoanApplication.AbstractBuilder<
                    MorabeheLoanApplicationId, MorabeheLoanApplication, Builder> {

        @Override
        protected MorabeheLoanApplication buildInternal() {
            return new MorabeheLoanApplication(this);
        }

        @Override
        protected void validateSpecificRules(Notification notification) {}
    }
}
