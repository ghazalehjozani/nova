package ir.dotin.loan.trade.core.application.ports.outbound.client.formula;

import java.math.BigDecimal;
import java.util.Map;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteReadPort;

public interface EvaluateFormulaViaFcbPort extends RemoteReadPort {

    Result<BigDecimal> evaluateViaFcb(String code, Map<String, BigDecimal> friendlyVars);
}
