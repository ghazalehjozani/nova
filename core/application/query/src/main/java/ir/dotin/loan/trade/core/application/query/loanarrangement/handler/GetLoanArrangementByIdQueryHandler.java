package ir.dotin.loan.trade.core.application.query.loanarrangement.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.query.loanarrangement.repository.TradeLoanArrangementQueryRepository;
import ir.dotin.loan.trade.core.application.query.loanarrangement.request.GetLoanArrangementByIdQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanArrangementByIdQueryHandler
        implements QueryHandler<GetLoanArrangementByIdQuery, TradeLoanArrangementQueryDto> {

    private final TradeLoanArrangementQueryRepository queryRepository;

    @Override
    public TradeLoanArrangementQueryDto handle(GetLoanArrangementByIdQuery query) {
        return queryRepository.findById(query.loanArrangementId()).orElseThrow();
    }
}
