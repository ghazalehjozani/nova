package ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate;

import ir.dotin.platform.domain.common.Notification;
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

    public static final class Builder
            extends AbstractLoanApplication.AbstractBuilder<
                    MorabeheLoanApplicationId, MorabeheLoanApplication, Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected MorabeheLoanApplication buildInternal() {
            return new MorabeheLoanApplication(this);
        }

        @Override
        protected void validateSpecificRules(Notification notification) {}
    }
}
