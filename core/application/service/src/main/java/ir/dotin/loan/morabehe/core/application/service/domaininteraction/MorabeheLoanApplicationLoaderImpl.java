package ir.dotin.loan.morabehe.core.application.service.domaininteraction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.core.domain.loanapplication.intraction.loader.MorabeheLoanApplicationLoader;

@Component
@Transactional(readOnly = true)
public class MorabeheLoanApplicationLoaderImpl implements MorabeheLoanApplicationLoader {

    @Override
    public boolean existByApplicationNumber(ApplicationNumber applicationNumber) {
        return false;
    }
}
