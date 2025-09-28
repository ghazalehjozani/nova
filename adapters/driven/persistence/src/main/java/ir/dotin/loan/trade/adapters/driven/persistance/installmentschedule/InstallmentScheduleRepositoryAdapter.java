package ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity.InstallmentEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.entity.InstallmentScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.mapper.InstallmentPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.mapper.InstallmentSchedulePersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.repository.InstallmentJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistance.installmentschedule.repository.InstallmentScheduleJpaRepository;
import ir.dotin.loan.trade.core.application.ports.driven.repository.InstallmentRepository;
import ir.dotin.loan.trade.core.application.ports.driven.repository.InstallmentScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InstallmentScheduleRepositoryAdapter implements InstallmentScheduleRepository, InstallmentRepository {

    private final InstallmentScheduleJpaRepository installmentScheduleJpaRepository;
    private final InstallmentJpaRepository installmentJpaRepository;
    private final InstallmentSchedulePersistenceMapper installmentScheduleMapper;
    private final InstallmentPersistenceMapper installmentMapper;

    @Override
    public Optional<InstallmentSchedule> findById(InstallmentScheduleId id) {
        return installmentScheduleJpaRepository.findById(id.value()).map(installmentScheduleMapper::map);
    }

    @Override
    public Optional<InstallmentSchedule> findByScheduleNumber(String scheduleNumber) {
        return installmentScheduleJpaRepository
                .findByScheduleNumber(scheduleNumber)
                .map(installmentScheduleMapper::map);
    }

    @Override
    public List<InstallmentSchedule> findByLoanFacilityId(LoanFacilityId loanFacilityId) {
        return installmentScheduleJpaRepository.findByLoanFacilityIdValue(loanFacilityId.value()).stream()
                .map(installmentScheduleMapper::map)
                .toList();
    }

    @Override
    public Optional<InstallmentSchedule> findActiveByLoanFacilityId(LoanFacilityId loanFacilityId) {
        return installmentScheduleJpaRepository
                .findActiveByLoanFacilityId(loanFacilityId.value())
                .map(installmentScheduleMapper::map);
    }

    @Override
    public List<InstallmentSchedule> findByStatus(InstallmentScheduleStatus status) {
        return installmentScheduleJpaRepository.findByStatus(status.name()).stream()
                .map(installmentScheduleMapper::map)
                .toList();
    }

    @Override
    public List<InstallmentSchedule> findAllActiveSchedules() {
        return installmentScheduleJpaRepository.findAllActiveSchedules().stream()
                .map(installmentScheduleMapper::map)
                .toList();
    }

    @Override
    @Transactional
    public InstallmentSchedule save(InstallmentSchedule installmentSchedule) {
        InstallmentScheduleEntity entity = installmentScheduleMapper.map(installmentSchedule);
        InstallmentScheduleEntity savedEntity = installmentScheduleJpaRepository.save(entity);
        return installmentScheduleMapper.map(savedEntity);
    }

    @Override
    @Transactional
    public void delete(InstallmentScheduleId id) {
        installmentScheduleJpaRepository.deleteById(id.value());
    }

    @Override
    public boolean existsByScheduleNumber(String scheduleNumber) {
        return installmentScheduleJpaRepository.existsByScheduleNumber(scheduleNumber);
    }

    @Override
    public boolean existsByLoanFacilityId(LoanFacilityId loanFacilityId) {
        return installmentScheduleJpaRepository.existsByLoanFacilityIdValue(loanFacilityId.value());
    }

    // InstallmentRepository methods
    @Override
    public Optional<Installment> findInstallmentById(UUID id) {
        return installmentJpaRepository.findById(id).map(installmentMapper::map);
    }

    @Override
    public List<Installment> findInstallmentsByScheduleId(InstallmentScheduleId scheduleId) {
        return installmentJpaRepository.findByInstallmentScheduleIdOrderBySequence(scheduleId.value()).stream()
                .map(installmentMapper::map)
                .toList();
    }

    @Override
    public List<Installment> findUnpaidInstallmentsByScheduleId(InstallmentScheduleId scheduleId) {
        return installmentJpaRepository.findUnpaidInstallmentsByScheduleId(scheduleId.value()).stream()
                .map(installmentMapper::map)
                .toList();
    }

    @Override
    public List<Installment> findOverdueInstallmentsByScheduleId(InstallmentScheduleId scheduleId) {
        return installmentJpaRepository.findOverdueInstallmentsByScheduleId(scheduleId.value()).stream()
                .map(installmentMapper::map)
                .toList();
    }

    @Override
    @Transactional
    public Installment saveInstallment(Installment installment) {
        InstallmentEntity entity = installmentMapper.map(installment);
        InstallmentEntity savedEntity = installmentJpaRepository.save(entity);
        return installmentMapper.map(savedEntity);
    }

    @Override
    @Transactional
    public void deleteInstallment(UUID id) {
        installmentJpaRepository.deleteById(id);
    }
}
