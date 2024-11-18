package ir.dotin.loan.core.domain.loanapplication.intraction.loader;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.platform.ddd.common.interaction.loader.AggregateRootLoader;

public interface MorabeheLoanApplicationLoader extends AggregateRootLoader {

    boolean existByApplicationNumber(ApplicationNumber applicationNumber);
}
