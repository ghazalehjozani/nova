package ir.dotin.loan.trade.core.application.query.loantype.request;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;

import lombok.Builder;

@Builder
public record GetLoanTypeByIdQuery(UUID loanTypeId) implements Query<TradeLoanTypeQueryDto> {
    @Override
    public Class<TradeLoanTypeQueryDto> getResultType() {
        return TradeLoanTypeQueryDto.class;
    }
}
