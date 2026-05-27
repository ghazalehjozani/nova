package ir.dotin.loan.trade.core.application.query.loanfacility.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.i18n.LoanFacilityQueryErrorCodes;
import ir.dotin.loan.trade.core.application.query.loanfacility.repository.TradeLoanFacilityQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.GetFacilityByIdQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFacilityByIdQueryHandler implements QueryHandler<GetFacilityByIdQuery, TradeFacilityQueryDto> {

    private final TradeLoanFacilityQueryRepository queryRepository;

    @Override
    public TradeFacilityQueryDto handle(GetFacilityByIdQuery query) {
        return queryRepository.findById(query.loanFacilityId()).orElseThrow(() -> {
            var notification =
                    Notification.ofError(LoanFacilityQueryErrorCodes.FACILITY_NOT_FOUND, query.loanFacilityId());
            return new FailureCauseException(FailureCause.notFound(notification));
        });
    }
}
