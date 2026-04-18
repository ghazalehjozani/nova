package ir.dotin.loan.trade.core.application.ports.outbound.client.samat;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;

public interface ValidateSamatPort {

    Result<Void> validateSamat(Samat samat, String loanTypeCode, String economicalSectionCode);
}
