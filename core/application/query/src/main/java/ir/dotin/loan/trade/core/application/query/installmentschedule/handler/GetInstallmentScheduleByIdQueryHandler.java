package ir.dotin.loan.trade.core.application.query.installmentschedule.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;
import ir.dotin.loan.trade.core.application.query.installmentschedule.repository.TradeInstallmentScheduleQueryRepository;
import ir.dotin.loan.trade.core.application.query.installmentschedule.request.GetInstallmentScheduleByIdQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetInstallmentScheduleByIdQueryHandler
        implements QueryHandler<GetInstallmentScheduleByIdQuery, TradeInstallmentScheduleQueryDto> {
    private final TradeInstallmentScheduleQueryRepository tradeInstallmentScheduleRepository;

    public TradeInstallmentScheduleQueryDto handle(GetInstallmentScheduleByIdQuery query) {
        return tradeInstallmentScheduleRepository
                .findById(query.installmentScheduleId())
                .orElseThrow();
    }
}
