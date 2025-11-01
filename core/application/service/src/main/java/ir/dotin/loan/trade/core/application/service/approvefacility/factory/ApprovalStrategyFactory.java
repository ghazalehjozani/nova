package ir.dotin.loan.trade.core.application.service.approvefacility.factory;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.service.approvefacility.strategy.ApprovalStrategy;
import ir.dotin.loan.trade.core.application.service.approvefacility.strategy.AutoApprovalStrategy;
import ir.dotin.loan.trade.core.application.service.approvefacility.strategy.ManualApprovalStrategy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApprovalStrategyFactory {

    private final AutoApprovalStrategy autoApprovalStrategy;
    private final ManualApprovalStrategy manualApprovalStrategy;

    public ApprovalStrategy getStrategy(ApproveFacilityCommand command) {
        return command.sanctionSerial() == null ? autoApprovalStrategy : manualApprovalStrategy;
    }
}
