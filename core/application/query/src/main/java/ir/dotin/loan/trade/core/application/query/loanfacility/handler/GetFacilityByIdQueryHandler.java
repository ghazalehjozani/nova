package ir.dotin.loan.trade.core.application.query.loanfacility.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.exception.BusinessRuleViolationException;
import ir.dotin.platform.dispatcher.api.query.QueryHandler;
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
            return new BusinessRuleViolationException(notification);
        });
    }
}
