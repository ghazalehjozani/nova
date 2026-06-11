package ir.dotin.loan.trade.core.application.ports.outbound.client;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemotePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;

public interface FetchSanctionDetailsPort extends RemotePort {
    Result<SanctionDetails> fetchBySanctionSerial(String sanctionSerial);
}
