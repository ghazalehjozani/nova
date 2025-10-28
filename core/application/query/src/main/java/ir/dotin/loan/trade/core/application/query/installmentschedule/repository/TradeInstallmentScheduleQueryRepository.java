package ir.dotin.loan.trade.core.application.query.installmentschedule.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;

public interface TradeInstallmentScheduleQueryRepository {
    Optional<TradeInstallmentScheduleQueryDto> findById(UUID id);
}
