package ir.dotin.loan.trade.core.application.query.loanfacility.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeLoanFacilityQueryPort;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.response.GetFacilityByIdQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFacilityByIdQueryHandler implements QueryHandler<GetFacilityByIdQuery, TradeFacilityQueryDto> {

    private final TradeLoanFacilityQueryPort queryRepository;

    @Override
    public TradeFacilityQueryDto handle(GetFacilityByIdQuery query) {
        return queryRepository
                .findById(query.loanFacilityId())
                .orElseThrow(); // Use custom Exception for fot found that use Notification
    }
}
