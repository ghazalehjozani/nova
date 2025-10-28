package ir.dotin.loan.trade.core.application.query.loanarrangement.request;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;

import lombok.Builder;

@Builder
public record GetLoanArrangementByIdQuery(UUID uid, UUID loanArrangementId)
        implements Query<TradeLoanArrangementQueryDto> {
    @Override
    public Class<TradeLoanArrangementQueryDto> getResultType() {
        return TradeLoanArrangementQueryDto.class;
    }
}
