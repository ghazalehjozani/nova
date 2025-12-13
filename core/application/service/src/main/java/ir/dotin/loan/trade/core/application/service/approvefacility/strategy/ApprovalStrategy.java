package ir.dotin.loan.trade.core.application.service.approvefacility.strategy;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface ApprovalStrategy {

    Result<Void> validate(ApproveFacilityCommand command, TradeLoanFacility facility, TradeLoanArrangement arrangement);

    Result<Void> approve(TradeLoanFacility facility, TradeLoanArrangement arrangement, ConfirmType confirmType);
}
