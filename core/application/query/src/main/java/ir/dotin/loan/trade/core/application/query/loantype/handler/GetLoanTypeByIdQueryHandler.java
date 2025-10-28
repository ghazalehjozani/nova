package ir.dotin.loan.trade.core.application.query.loantype.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.repository.TradeLoanTypeQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantype.request.GetLoanTypeByIdQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanTypeByIdQueryHandler implements QueryHandler<GetLoanTypeByIdQuery, TradeLoanTypeQueryDto> {

    private final TradeLoanTypeQueryRepository tradeLoanTypeRepository;

    public TradeLoanTypeQueryDto handle(GetLoanTypeByIdQuery query) {
        return tradeLoanTypeRepository.findById(query.loanTypeId()).orElseThrow();
    }
}
