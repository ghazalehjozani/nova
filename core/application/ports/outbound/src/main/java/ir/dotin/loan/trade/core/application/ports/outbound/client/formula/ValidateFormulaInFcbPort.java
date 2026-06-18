package ir.dotin.loan.trade.core.application.ports.outbound.client.formula;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteReadPort;

public interface ValidateFormulaInFcbPort extends RemoteReadPort {

    Result<Unit> validateFormulaInFcb(String code);
}
