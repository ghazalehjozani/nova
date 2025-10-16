package ir.dotin.loan.trade.core.application.ports.outbound.query.response;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeInstallmentScheduleQueryDto;

import lombok.Builder;

@Builder
public record GetInstallmentScheduleByIdQuery(UUID uid, UUID installmentScheduleId)
        implements Query<TradeInstallmentScheduleQueryDto> {
    @Override
    public Class<TradeInstallmentScheduleQueryDto> getResultType() {
        return TradeInstallmentScheduleQueryDto.class;
    }
}
