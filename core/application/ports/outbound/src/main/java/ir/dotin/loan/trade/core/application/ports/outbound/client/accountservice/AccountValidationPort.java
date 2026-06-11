package ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteReadPort;

public interface AccountValidationPort extends RemoteReadPort {

    Result<AccountNumber> validateAccountNumber(String accountNumber);
}
