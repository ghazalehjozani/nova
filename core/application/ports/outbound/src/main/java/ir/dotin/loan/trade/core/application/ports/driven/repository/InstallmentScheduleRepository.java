package ir.dotin.loan.trade.core.application.ports.driven.repository;

import java.util.Optional;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;

public interface InstallmentScheduleRepository {

    InstallmentSchedule save(InstallmentSchedule installmentSchedule);

    Optional<InstallmentSchedule> findById(InstallmentScheduleId id);
}
