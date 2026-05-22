package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.pangaea.persistence.jpa.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleEntity;

@Repository
public interface InstallmentScheduleJpaRepository extends PersistentRepository<InstallmentScheduleEntity> {}
