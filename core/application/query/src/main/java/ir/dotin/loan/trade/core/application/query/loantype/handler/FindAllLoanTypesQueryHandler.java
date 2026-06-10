package ir.dotin.loan.trade.core.application.query.loantype.handler;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loantype.dto.LoanTypeQueryResult;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.repository.TradeLoanTypeQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantype.request.FindAllLoanTypesQuery;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPage;
import ir.dotin.loan.trade.core.application.query.shared.pagination.CursorPageRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FindAllLoanTypesQueryHandler implements QueryHandler<FindAllLoanTypesQuery, LoanTypeQueryResult> {

    private final TradeLoanTypeQueryRepository tradeLoanTypeQueryPort;

    @Override
    public LoanTypeQueryResult handle(FindAllLoanTypesQuery query) {
        CursorPageRequest pageRequest = CursorPageRequest.of(query.cursor(), query.pageSize());
        CursorPage<TradeLoanTypeQueryDto> page = tradeLoanTypeQueryPort.findAll(pageRequest);

        return LoanTypeQueryResult.forCursor(
                page.content(), page.nextCursor(), page.previousCursor(), page.hasNext(), page.hasPrevious());
    }
}
