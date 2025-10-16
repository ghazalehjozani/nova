package ir.dotin.loan.trade.core.application.ports.outbound.query.response;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeLoanTypeQueryDto;

import lombok.Builder;

@Builder
public record GetLoanTypeByIdQuery(UUID uid, UUID loanTypeId) implements Query<TradeLoanTypeQueryDto> {
    @Override
    public Class<TradeLoanTypeQueryDto> getResultType() {
        return TradeLoanTypeQueryDto.class;
    }
}
