package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import java.util.List;

import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

public record FacilityOriginationContext(
        TradeLoanArrangement arrangement, TradeLoanType loanType, List<PartyInfoResponse> partyInfos) {

    public PartyInfoResponse primaryApplicant() {
        return partyInfos.stream()
                .filter(info -> info.party().partyRole() == PartyRole.PRIMARY_APPLICANT)
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("Invariant Failure: Primary applicant missing from context"));
    }
}
