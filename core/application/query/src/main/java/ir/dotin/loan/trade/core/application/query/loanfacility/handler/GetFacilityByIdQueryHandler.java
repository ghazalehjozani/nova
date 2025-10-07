package ir.dotin.loan.trade.core.application.query.loanfacility.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.ports.inbound.query.GetFacilityByIdQuery;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.FacilityQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeLoanFacilityQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFacilityByIdQueryHandler implements QueryHandler<GetFacilityByIdQuery, FacilityQueryDto> {

    private final TradeLoanFacilityQueryRepository queryRepository;

    @Override
    public FacilityQueryDto handle(GetFacilityByIdQuery query) {
        return queryRepository
                .findById(query.loanFacilityId())
                .orElseThrow(); // Use custom Exception for fot found that use Notification
    }
}
