package ir.dotin.loan.morabehe.core.application.service.usecase;


import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;

public interface CreateLoanApplicationUseCase {

    MorabeheLoanApplication create(MorabeheLoanApplication loanApplication);

}
