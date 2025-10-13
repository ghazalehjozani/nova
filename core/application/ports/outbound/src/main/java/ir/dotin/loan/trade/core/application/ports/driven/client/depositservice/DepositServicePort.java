package ir.dotin.loan.trade.core.application.ports.driven.client.depositservice;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;

public interface DepositServicePort {

    Result<DepositInfo> getDepositInfo(DepositNumber depositNumber);
}
