package ir.dotin.loan.trade.core.application.ports.driven.client;

import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;

public interface BankPort {

    BranchCode getCurrentBranchCode();
}
