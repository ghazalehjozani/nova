package ir.dotin.loan.trade.core.application.service.approvefacility.component;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;

@Component
public class SanctionDetailsLoader {

    private final FetchSanctionDetailsPort fetchSanctionDetailsPort;

    public SanctionDetailsLoader(FetchSanctionDetailsPort fetchSanctionDetailsPort) {
        this.fetchSanctionDetailsPort = fetchSanctionDetailsPort;
    }

    public Result<SanctionDetails> loadForManualApproval(UUID loanFacilityId) {
        return fetchSanctionDetailsPort.fetchBySanctionSerial(loanFacilityId.toString());
    }
}
