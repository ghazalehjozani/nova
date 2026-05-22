package ir.dotin.loan.trade.core.application.query.loanfacility.handler;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.LoanFacilityQueryResult;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.repository.TradeLoanFacilityQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.FindAllLoanFacilitiesQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FindAllLoanFacilitiesQueryHandler
        implements QueryHandler<FindAllLoanFacilitiesQuery, LoanFacilityQueryResult> {

    private final TradeLoanFacilityQueryRepository tradeLoanFacilityQueryPort;

    @Override
    public LoanFacilityQueryResult handle(FindAllLoanFacilitiesQuery query) {
        CursorPageRequest pageRequest = CursorPageRequest.of(query.cursor(), query.pageSize());
        CursorPage<TradeFacilityQueryDto> page = tradeLoanFacilityQueryPort.findAll(pageRequest);

        return LoanFacilityQueryResult.forCursor(
                page.content(), page.nextCursor(), page.previousCursor(), page.hasNext(), page.hasPrevious());
    }
}
