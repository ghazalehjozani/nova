package ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice;

import java.util.List;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation;

public interface CollateralServicePort {
    Result<CollateralValidation> validateAddAssuranceToFile(
            List<CollateralSerial> collateralSerial, List<Long> usedCosts);
}
