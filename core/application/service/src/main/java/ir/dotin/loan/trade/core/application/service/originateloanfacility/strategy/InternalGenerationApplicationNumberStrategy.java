package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InternalGenerationApplicationNumberStrategy implements ApplicationNumberStrategy {

    @Override
    public @NonNull Result<ApplicationNumber> generateOrValidateApplicationNumber(
            @NonNull Branch branch,
            @NonNull LoanTypeCode loanTypeCode,
            @NonNull Party mainCustomer,
            @NonNull String derivedSequence) {

        ApplicationNumber applicationNumber =
                new ApplicationNumber(branch, loanTypeCode, mainCustomer, derivedSequence);

        return Result.success(applicationNumber);
    }

    @Override
    public @NonNull ApplicationNumberGenerationType getType() {
        return ApplicationNumberGenerationType.INTERNAL_GENERATION;
    }
}
