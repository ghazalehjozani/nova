package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityPersister {

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;

    public Result<OriginationResult> persist(TradeLoanFacility facility, Optional<InstallmentSchedule> scheduleOpt) {
        scheduleOpt.ifPresent(schedule -> {
            InstallmentSchedule saved = installmentScheduleRepository.save(schedule);
            log.debug("Installment schedule persisted: {}", saved.getId().value());
        });

        TradeLoanFacility savedFacility = loanFacilityRepository.save(facility);
        log.info("Facility persisted: {}", savedFacility.getId().value());

        return Result.success(new OriginationResult(facility, scheduleOpt));
    }

    public List<DomainEvent<?>> aggregateEvents(OriginationResult result) {
        List<DomainEvent<?>> allEvents = new ArrayList<>(result.facility().domainEvents());
        result.installmentSchedule().ifPresent(schedule -> allEvents.addAll(schedule.domainEvents()));
        return allEvents;
    }

    public record OriginationResult(TradeLoanFacility facility, Optional<InstallmentSchedule> installmentSchedule) {}
}
