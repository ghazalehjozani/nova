package ir.dotin.loan.morabehe.core.application.service.loader;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.core.domain.loanapplication.intraction.loader.MorabeheLoanApplicationLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class MorabeheLoanApplicationLoaderImpl implements MorabeheLoanApplicationLoader {

    @Override
    public boolean existByApplicationNumber(ApplicationNumber applicationNumber) {
        return false;
    }
}
