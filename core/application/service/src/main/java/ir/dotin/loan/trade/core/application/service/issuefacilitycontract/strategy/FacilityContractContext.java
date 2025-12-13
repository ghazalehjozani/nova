package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.strategy;

import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

public record FacilityContractContext(TradeLoanFacility facility, TradeLoanArrangement arrangement) {

    public TradeLoanFacility facility() {
        return facility;
    }

    public TradeLoanArrangement arrangement() {
        return arrangement;
    }
}
