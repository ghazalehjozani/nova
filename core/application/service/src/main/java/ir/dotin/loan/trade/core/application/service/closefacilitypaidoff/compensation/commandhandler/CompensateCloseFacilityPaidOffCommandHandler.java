package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.compensation.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCloseFacilityPaidOffCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompensateCloseFacilityPaidOffCommandHandler
        implements CommandHandler<CompensateCloseFacilityPaidOffCommand> {

    private static final Logger log = LoggerFactory.getLogger(CompensateCloseFacilityPaidOffCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final ApplicationNumberResolver applicationNumberResolver;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(CompensateCloseFacilityPaidOffCommand command) {
        List<DomainEvent<?>> allEvents = new ArrayList<>();

        return resolveIdentifiers(command)
                .flatMap(ids -> loadSchedule(ids)
                        .flatMap(schedule -> schedule.revertCollectInstallment(
                                        List.of(command.transactionReference()), clock)
                                .map(__ -> schedule))
                        .peekValue(schedule -> allEvents.addAll(schedule.domainEvents()))
                        .peekValue(installmentScheduleRepository::save)
                        .peekValue(schedule -> log.info(
                                "Reverted installment schedule for compensate: applicationNumber={}, ref={}",
                                command.applicationNumber(),
                                command.transactionReference()))
                        .flatMap(__ -> loadFacility(ids))
                        .flatMap(facility -> facility.revertClosePaidOff(command.transactionReference(), clock)
                                .map(v -> facility))
                        .peekValue(facility -> allEvents.addAll(facility.domainEvents()))
                        .peekValue(facility -> {
                            repository.save(facility);
                            log.info(
                                    "Reverted close paid off for facility: applicationNumber={}, ref={}",
                                    command.applicationNumber(),
                                    command.transactionReference());
                        }))
                .mapNonNull(__ -> allEvents);
    }

    private Result<ApplicationNumberResolver.LoanIdentifiers> resolveIdentifiers(
            CompensateCloseFacilityPaidOffCommand command) {
        return Result.fromOptional(
                applicationNumberResolver.resolveByApplicationNumber(command.applicationNumber()),
                () -> Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber()));
    }

    private Result<InstallmentSchedule> loadSchedule(ApplicationNumberResolver.LoanIdentifiers ids) {
        return Result.fromOptional(
                installmentScheduleRepository.findById(
                        InstallmentScheduleId.of(ids.installmentScheduleId()).getValue()),
                () -> Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND, ids.installmentScheduleId()));
    }

    private Result<TradeLoanFacility> loadFacility(ApplicationNumberResolver.LoanIdentifiers ids) {
        return Result.fromOptional(
                repository.findById(LoanFacilityId.of(ids.loanFacilityId())),
                () -> Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, ids.loanFacilityId()));
    }
}
