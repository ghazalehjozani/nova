package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;

public interface ApproveLoanApplicationUseCase {

    MorabeheLoanApplication approve(MorabeheLoanApplication loanApplication,
                                    SanctionSerial sanctionSerial);
}
