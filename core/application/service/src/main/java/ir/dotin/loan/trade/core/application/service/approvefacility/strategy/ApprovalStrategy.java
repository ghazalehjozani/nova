package ir.dotin.loan.trade.core.application.service.approvefacility.strategy;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public interface ApprovalStrategy {

    Result<Unit> validate(ApproveFacilityCommand command, TradeLoanFacility facility, TradeLoanArrangement arrangement);

    Result<Unit> approve(
            ApproveFacilityCommand command,
            TradeLoanFacility facility,
            TradeLoanArrangement arrangement,
            ConfirmType confirmType,
            @Nullable SanctionDetails sanctionDetails);
}
