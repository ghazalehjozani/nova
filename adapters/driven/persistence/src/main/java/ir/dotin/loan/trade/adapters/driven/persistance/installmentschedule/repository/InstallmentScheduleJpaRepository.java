package ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity.InstallmentScheduleEntity;

@Repository
public interface InstallmentScheduleJpaRepository extends PersistentRepository<InstallmentScheduleEntity> {}
