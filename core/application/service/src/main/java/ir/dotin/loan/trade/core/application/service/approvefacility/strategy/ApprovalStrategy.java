package ir.dotin.loan.trade.core.application.service.approvefacility.strategy;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface ApprovalStrategy {

    Result<Unit> validate(ApproveFacilityCommand command, TradeLoanFacility facility, TradeLoanArrangement arrangement);

    /**
     * Approves the facility. The {@code command} is passed so the manual strategy can read the FCB-resolved
     * {@code sanctionDetails} threaded onto it by the pre-flight ({@code PrepareFacilityApprovalQuery}) instead of
     * re-issuing an FCB read. The auto strategy ignores it.
     */
    Result<Unit> approve(
            ApproveFacilityCommand command,
            TradeLoanFacility facility,
            TradeLoanArrangement arrangement,
            ConfirmType confirmType);
}
