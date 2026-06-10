package ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice;

import java.util.List;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;

public interface BranchCoveragePort {

    Result<List<BranchCode>> coveredBranches(BranchCode branchCode);
}
