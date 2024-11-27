package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;

public interface CreateLoanTypeUseCase {

    MorabeheLoanType create(MorabeheLoanType morabeheLoanType);
}
