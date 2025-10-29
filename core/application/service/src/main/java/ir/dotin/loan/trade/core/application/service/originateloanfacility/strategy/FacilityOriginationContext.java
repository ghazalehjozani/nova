package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import java.util.List;

import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfo;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

public record FacilityOriginationContext(
        TradeLoanArrangement arrangement, TradeLoanType loanType, PartyInfo mainCustomer, List<PartyInfo> guarantors) {}
