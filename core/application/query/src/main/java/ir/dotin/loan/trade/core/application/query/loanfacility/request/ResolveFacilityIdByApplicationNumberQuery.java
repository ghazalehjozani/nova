package ir.dotin.loan.trade.core.application.query.loanfacility.request;

import ir.dotin.platform.pangaea.servicelayer.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.FacilityIdView;

import lombok.Builder;

@Builder
public record ResolveFacilityIdByApplicationNumberQuery(String applicationNumber) implements Query<FacilityIdView> {

    @Override
    public Class<FacilityIdView> getResultType() {
        return FacilityIdView.class;
    }
}
