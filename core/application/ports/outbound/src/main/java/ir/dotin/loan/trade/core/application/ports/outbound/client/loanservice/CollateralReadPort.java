package ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice;

import java.util.List;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteReadPort;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation;

public interface CollateralReadPort extends RemoteReadPort {

    Result<CollateralValidation> validateAddAssuranceToFile(
            List<CollateralSerial> collateralSerial, List<Long> usedCosts, BranchCode branchCode);

    Result<CollateralDetails> loadCollateral(String assuranceSerial, String uniqueTrackingCode);
}
