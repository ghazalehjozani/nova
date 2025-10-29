package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.mapper.InstallmentSchedulePersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository.InstallmentScheduleJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InstallmentScheduleRepositoryAdapter implements InstallmentScheduleRepository {

    private final InstallmentScheduleJpaRepository installmentScheduleJpaRepository;
    private final InstallmentSchedulePersistenceMapper installmentScheduleMapper;

    @Override
    @Transactional
    public InstallmentSchedule save(InstallmentSchedule installmentSchedule) {
        var scheduleEntity = installmentScheduleMapper.map(installmentSchedule);
        scheduleEntity.getInstallments().forEach(installment -> installment.setInstallmentSchedule(scheduleEntity));
        var saved = installmentScheduleJpaRepository.save(scheduleEntity);
        return installmentScheduleMapper.map(saved).build();
    }

    @Override
    public Optional<InstallmentSchedule> findById(InstallmentScheduleId id) {
        return installmentScheduleJpaRepository
                .findById(id.value())
                .map(entity -> installmentScheduleMapper.map(entity).build());
    }
}
