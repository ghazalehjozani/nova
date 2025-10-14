package ir.dotin.loan.trade.core.application.query.installmentschedule.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.ports.inbound.query.GetInstallmentScheduleByIdQuery;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.TradeInstallmentScheduleQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeInstallmentScheduleQueryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetInstallmentScheduleByIdQueryHandler
        implements QueryHandler<GetInstallmentScheduleByIdQuery, TradeInstallmentScheduleQueryDto> {
    private final TradeInstallmentScheduleQueryPort tradeInstallmentScheduleRepository;

    public TradeInstallmentScheduleQueryDto handle(GetInstallmentScheduleByIdQuery query) {
        return tradeInstallmentScheduleRepository
                .findById(query.installmentScheduleId())
                .orElseThrow();
    }
}
