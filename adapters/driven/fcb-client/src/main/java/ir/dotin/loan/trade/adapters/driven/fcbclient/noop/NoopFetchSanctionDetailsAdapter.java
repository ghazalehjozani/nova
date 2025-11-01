package ir.dotin.loan.trade.adapters.driven.fcbclient.noop;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;

@Component
public class NoopFetchSanctionDetailsAdapter implements FetchSanctionDetailsPort {

    @Override
    public Result<SanctionDetails> fetchBySanctionSerial(String sanctionSerial) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
