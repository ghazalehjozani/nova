package ir.dotin.loan.trade.core.application.ports.outbound.client;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;

public interface FetchSanctionDetailsPort {
    Result<SanctionDetails> fetchBySanctionSerial(String sanctionSerial);
}
