package ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.component;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.mapper.DefineTradeLoanArrangementCommandMapper;

@Component
public class EconomicalSectorLoader {

    private final DefineTradeLoanArrangementCommandMapper mapper;
    private final LoanServicePort loanServicePort;

    public EconomicalSectorLoader(DefineTradeLoanArrangementCommandMapper mapper, LoanServicePort loanServicePort) {
        this.mapper = mapper;
        this.loanServicePort = loanServicePort;
    }

    public Result<EconomicSector> loadForArrangement(DefineTradeLoanArrangementCommand command) {
        return loanServicePort.loadEconomicalSectorByCode(mapper.map(command.economicSector()));
    }
}
