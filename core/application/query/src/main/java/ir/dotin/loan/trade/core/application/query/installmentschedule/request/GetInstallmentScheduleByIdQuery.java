package ir.dotin.loan.trade.core.application.query.installmentschedule.request;

import java.util.UUID;

import ir.dotin.platform.pangaea.dispatcher.api.query.Query;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;

import lombok.Builder;

@Builder
public record GetInstallmentScheduleByIdQuery(UUID installmentScheduleId)
        implements Query<TradeInstallmentScheduleQueryDto> {
    @Override
    public Class<TradeInstallmentScheduleQueryDto> getResultType() {
        return TradeInstallmentScheduleQueryDto.class;
    }
}
