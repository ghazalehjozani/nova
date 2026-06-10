package ir.dotin.loan.trade.core.application.query.loanarrangement.handler;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.LoanArrangementQueryResult;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.repository.TradeLoanArrangementQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.FindAllLoanArrangementsQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FindAllLoanArrangementsQueryHandler
        implements QueryHandler<FindAllLoanArrangementsQuery, LoanArrangementQueryResult> {

    private final TradeLoanArrangementQueryRepository queryRepository;

    @Override
    public LoanArrangementQueryResult handle(FindAllLoanArrangementsQuery query) {
        CursorPageRequest pageRequest = CursorPageRequest.of(query.cursor(), query.pageSize());
        CursorPage<TradeLoanArrangementQueryDto> page = queryRepository.findAll(pageRequest);

        return LoanArrangementQueryResult.forCursor(
                page.content(), page.nextCursor(), page.previousCursor(), page.hasNext(), page.hasPrevious());
    }
}
