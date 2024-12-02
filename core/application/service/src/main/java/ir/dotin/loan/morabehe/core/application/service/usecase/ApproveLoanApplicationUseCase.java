package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;

public interface ApproveLoanApplicationUseCase {

    MorabeheLoanApplication approve(MorabeheLoanApplicationId loanApplicationId,
                                    SanctionSerial sanctionSerial);
}
