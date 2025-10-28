package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query.mapper.InstallmentScheduleQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository.InstallmentScheduleJpaRepository;
import ir.dotin.loan.trade.core.application.query.installmentschedule.dto.TradeInstallmentScheduleQueryDto;
import ir.dotin.loan.trade.core.application.query.installmentschedule.repository.TradeInstallmentScheduleQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JpaTradeInstallmentScheduleQueryAdapter implements TradeInstallmentScheduleQueryRepository {
    private final InstallmentScheduleJpaRepository jpaRepository;
    private final InstallmentScheduleQueryModelMapper queryModelMapper;

    @Override
    public Optional<TradeInstallmentScheduleQueryDto> findById(UUID installmentScheduleId) {
        return jpaRepository.findById(installmentScheduleId).map(queryModelMapper::toDto);
    }
}
