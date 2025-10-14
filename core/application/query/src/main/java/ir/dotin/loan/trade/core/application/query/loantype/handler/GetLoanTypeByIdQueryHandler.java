package ir.dotin.loan.trade.core.application.query.loantype.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.ports.inbound.query.GetLoanTypeByIdQuery;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeLoanTypeQueryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanTypeByIdQueryHandler implements QueryHandler<GetLoanTypeByIdQuery, TradeLoanTypeQueryDto> {

    private final TradeLoanTypeQueryPort tradeLoanTypeRepository;

    public TradeLoanTypeQueryDto handle(GetLoanTypeByIdQuery query) {
        return tradeLoanTypeRepository.findById(query.loanTypeId()).orElseThrow();
    }
}
