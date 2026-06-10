package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;

public interface ApplicationNumberGenerationStrategy {

    Result<ApplicationNumber> generateApplicationNumber(
            Branch branch, LoanTypeCode loanTypeCode, Party primaryApplicant);

    ApplicationNumberGenerationType getType();
}
