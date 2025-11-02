package ir.dotin.loan.trade.core.application.query.loantype.handler;

import org.springframework.stereotype.Component;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loantype.dto.LoanTypeQueryResult;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.repository.TradeLoanTypeQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantype.request.LoanTypeFilterQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.OffsetPage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanTypeFilterQueryHandler implements QueryHandler<LoanTypeFilterQuery, LoanTypeQueryResult> {

    private final TradeLoanTypeQueryRepository tradeLoanTypeQueryPort;

    @Override
    public LoanTypeQueryResult handle(LoanTypeFilterQuery query) {
        OffsetPage<TradeLoanTypeQueryDto> page = tradeLoanTypeQueryPort.findByFilter(query);

        return LoanTypeQueryResult.forOffset(
                page.content(),
                page.currentPage(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext(),
                page.hasPrevious());
    }
}
