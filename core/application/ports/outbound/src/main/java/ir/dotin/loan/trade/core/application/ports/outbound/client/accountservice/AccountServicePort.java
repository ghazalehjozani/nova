package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;

public interface AccountServicePort {

    Result<AccountInfo> openAccount(LoanTopic loanTopic);
}
