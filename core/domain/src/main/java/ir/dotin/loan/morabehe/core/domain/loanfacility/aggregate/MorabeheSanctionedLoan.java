package ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate;

import java.util.Objects;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.aggregate.AbstractSanctionedLoan;
import ir.dotin.loan.morabehe.core.domain.loanfacility.vo.MorabeheSanctionedLoanId;

public final class MorabeheSanctionedLoan
        extends AbstractSanctionedLoan<MorabeheSanctionedLoanId, MorabeheSanctionedLoan.Builder> {

    private MorabeheSanctionedLoan(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    static Result<MorabeheSanctionedLoan> reconstitute(Builder builder) {
        Objects.requireNonNull(builder, "Builder cannot be null for reconstitution.");
        return builder.build();
    }

    public static final class Builder
            extends AbstractSanctionedLoan.AbstractBuilder<MorabeheSanctionedLoanId, MorabeheSanctionedLoan, Builder> {

        @Override
        protected MorabeheSanctionedLoan buildInternal() {
            return new MorabeheSanctionedLoan(this);
        }

        @Override
        protected void validateSpecificRules(Notification notification) {}
    }
}
