package ir.dotin.loan.trade.core.application.ports.outbound.client.samat;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteReadPort;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;

public interface ValidateSamatPort extends RemoteReadPort {

    Result<Unit> validateSamat(Samat samat, String loanTypeCode, String economicalSectionCode);
}
