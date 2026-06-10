package ir.dotin.loan.trade.core.application.query.installmentschedule.handler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;
import ir.dotin.loan.trade.core.application.query.installmentschedule.i18n.InstallmentScheduleQueryErrorCodes;
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
                .orElseThrow(() -> {
                    var notification = Notification.ofError(
                            InstallmentScheduleQueryErrorCodes.INSTALLMENT_SCHEDULE_NOT_FOUND,
                            query.installmentScheduleId());
                    return new FailureCauseException(FailureCause.notFound(notification));
                });
    }
}
