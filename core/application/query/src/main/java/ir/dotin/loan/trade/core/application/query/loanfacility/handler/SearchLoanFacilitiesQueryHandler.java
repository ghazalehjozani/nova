package ir.dotin.loan.trade.core.application.query.loanfacility.handler;

import org.springframework.stereotype.Component;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.LoanFacilityQueryResult;
import ir.dotin.loan.trade.core.application.query.loanfacility.dto.TradeFacilityQueryDto;
import ir.dotin.loan.trade.core.application.query.loanfacility.repository.TradeLoanFacilityQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanfacility.request.LoanFacilityFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchLoanFacilitiesQueryHandler
        implements QueryHandler<LoanFacilityFilterQuery, LoanFacilityQueryResult> {

    private final TradeLoanFacilityQueryRepository tradeLoanFacilityQueryPort;

    @Override
    public LoanFacilityQueryResult handle(LoanFacilityFilterQuery query) {

        OffsetPage<TradeFacilityQueryDto> page = tradeLoanFacilityQueryPort.findByFilter(query);

        return LoanFacilityQueryResult.forOffset(
                page.content(),
                page.currentPage(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext(),
                page.hasPrevious());
    }
}
