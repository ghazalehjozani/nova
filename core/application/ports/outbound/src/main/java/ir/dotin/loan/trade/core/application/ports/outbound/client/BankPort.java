package ir.dotin.loan.trade.core.application.ports.outbound.client;

import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;

public interface BankPort {

    BranchCode getCurrentBranchCode();
}
