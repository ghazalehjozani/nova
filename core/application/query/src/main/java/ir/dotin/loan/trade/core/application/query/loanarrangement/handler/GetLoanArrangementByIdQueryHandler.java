package ir.dotin.loan.trade.core.application.query.loanarrangement.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeLoanArrangementQueryPort;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.response.GetLoanArrangementByIdQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanArrangementByIdQueryHandler
        implements QueryHandler<GetLoanArrangementByIdQuery, TradeLoanArrangementQueryDto> {

    private final TradeLoanArrangementQueryPort queryRepository;

    @Override
    public TradeLoanArrangementQueryDto handle(GetLoanArrangementByIdQuery query) {
        return queryRepository.findById(query.loanArrangementId()).orElseThrow();
    }
}
