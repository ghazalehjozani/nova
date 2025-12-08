package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import org.jspecify.annotations.NonNull;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;

public interface ApplicationNumberStrategy {

    @NonNull
    Result<ApplicationNumber> generateApplicationNumber(
            @NonNull Branch branch, @NonNull LoanTypeCode loanTypeCode, @NonNull Party primaryApplicant);

    @NonNull
    ApplicationNumberGenerationType getType();
}
