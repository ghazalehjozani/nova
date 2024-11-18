package ir.dotin.loan.core.domain.config.service;

import ir.dotin.loan.core.domain.config.entity.loantype.MorabeheLoanType;

public interface MorabeheLoanTypeUpdateService {

    MorabeheLoanType update(MorabeheLoanType newNewType, MorabeheLoanType oldNewType);

}
