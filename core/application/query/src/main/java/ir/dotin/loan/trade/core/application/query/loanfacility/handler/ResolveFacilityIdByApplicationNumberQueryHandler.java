package ir.dotin.loan.trade.core.application.query.loanfacility.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.FacilityIdView;
import ir.dotin.loan.trade.core.application.query.loanfacility.i18n.LoanFacilityQueryErrorCodes;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.ResolveFacilityIdByApplicationNumberQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResolveFacilityIdByApplicationNumberQueryHandler
        implements QueryHandler<ResolveFacilityIdByApplicationNumberQuery, FacilityIdView> {

    private final ApplicationNumberResolver applicationNumberResolver;

    @Override
    public FacilityIdView handle(ResolveFacilityIdByApplicationNumberQuery query) {
        LoanFacilityId facilityId = applicationNumberResolver
                .resolveLoanFacilityIdByApplicationNumber(query.applicationNumber())
                .orElseThrow(() -> {
                    var notification = Notification.ofError(
                            LoanFacilityQueryErrorCodes.FACILITY_NOT_FOUND, query.applicationNumber());
                    return new FailureCauseException(FailureCause.notFound(notification));
                });
        return new FacilityIdView(facilityId.value());
    }
}
