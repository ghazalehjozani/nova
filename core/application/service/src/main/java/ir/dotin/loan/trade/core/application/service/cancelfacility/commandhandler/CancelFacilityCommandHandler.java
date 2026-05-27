package ir.dotin.loan.trade.core.application.service.cancelfacility.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelFacilityCommandHandler implements CommandHandler<CancelFacilityCommand> {

    private static final Logger log = LoggerFactory.getLogger(CancelFacilityCommandHandler.class);

    private final TradeLoanFacilityRepository facilityRepository;
    private final InstallmentScheduleRepository scheduleRepository;
    private final TradeLoanFacilityService domainService;
    private final ApplicationNumberResolver applicationNumberResolver;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(CancelFacilityCommand command) {
        List<DomainEvent<?>> domainEvents = new ArrayList<>();

        return resolveIdentifiers(command)
                .flatMap(this::loadTradeLoanFacility)
                .flatMap(facility -> cancelFacility(facility, command, domainEvents))
                .flatMap(facility -> cancelInstallmentSchedule(facility, command, domainEvents))
                .map(unused -> domainEvents);
    }

    private Result<TradeLoanFacility> cancelFacility(
            TradeLoanFacility facility, CancelFacilityCommand command, List<DomainEvent<?>> domainEvents) {
        return domainService
                .cancel(
                        facility,
                        command.cancelDescription(),
                        command.cancelReason(),
                        command.cancelDate(),
                        command.cancelLoanTransactionNumber())
                .map(v -> {
                    facilityRepository.save(facility);
                    log.debug("Facility cancelled: {}", command.loanFacilityId());
                    domainEvents.addAll(facility.domainEvents());
                    return facility;
                });
    }

    private Result<TradeLoanFacility> cancelInstallmentSchedule(
            TradeLoanFacility facility, CancelFacilityCommand command, List<DomainEvent<?>> domainEvents) {
        Optional<InstallmentScheduleId> scheduleId = facility.getInstallmentScheduleId();
        if (scheduleId.isEmpty()) {
            return Result.failure(
                    Notification.ofError(TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND));
        }

        return Result.fromOptional(
                        scheduleRepository.findById(scheduleId.orElse(null)),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                                command.loanFacilityId())))
                .flatMap(schedule -> schedule.cancelSchedule(command.cancelReason(), clock)
                        .map(v -> {
                            scheduleRepository.save(schedule);
                            domainEvents.addAll(schedule.domainEvents());
                            return facility;
                        }));
    }

    private Result<ApplicationNumberResolver.LoanIdentifiers> resolveIdentifiers(CancelFacilityCommand command) {
        return Result.fromOptional(
                applicationNumberResolver.resolveByApplicationNumber(command.applicationNumber()),
                () -> FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private Result<TradeLoanFacility> loadTradeLoanFacility(ApplicationNumberResolver.LoanIdentifiers ids) {
        return Result.fromOptional(
                facilityRepository.findById(LoanFacilityId.of(ids.loanFacilityId())),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND, ids.loanFacilityId())));
    }
}
