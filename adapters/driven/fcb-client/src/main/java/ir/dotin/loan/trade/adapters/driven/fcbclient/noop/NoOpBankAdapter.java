package ir.dotin.loan.trade.adapters.driven.fcbclient.noop;

import org.springframework.stereotype.Component;

import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.trade.core.application.ports.outbound.client.BankPort;

@Component
public class NoOpBankAdapter implements BankPort {
    @Override
    public BranchCode getCurrentBranchCode() {
        return BranchCode.of("1").getValue();
    }
}
