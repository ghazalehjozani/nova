package ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteWritePort;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;

public interface CollateralServicePort extends RemoteWritePort {
    Result<List<CollateralSerial>> reserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID requestId,
            Integer reserveDurationMin,
            Money usedAmount);

    Result<CollateralSerial> unReserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID transactionId,
            UUID rollBackId);
}
