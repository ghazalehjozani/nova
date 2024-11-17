package ir.dotin.loan.core.domain.config.entity.loantype;


import ir.dotin.loan.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.platform.ddd.common.entity.AggregateRoot;

public class MorabeheLoanType extends AggregateRoot<MorabeheLoanTypeId> {

    private LoanType LoanType;

    protected MorabeheLoanType(MorabeheLoanTypeId morabeheLoanTypeId) {
        super(morabeheLoanTypeId);
    }

}
