package ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation;

public interface CollateralServicePort {
    Result<CollateralValidation> validateAddAssuranceToFile(
            List<CollateralSerial> collateralSerial, List<Long> usedCosts, BranchCode branchCode);

    Result<List<CollateralSerial>> reserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID requestId,
            Integer reserveDurationMin,
            Money usedAmount);

    Result<CollateralDetails> loadCollateral(String assuranceSerial, String uniqueTrackingCode);

    Result<CollateralSerial> unReserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID transactionId,
            UUID rollBackId);
}
