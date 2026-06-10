package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.compensation.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteCommandHandler;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteTransaction;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCloseFacilityPaidOffCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Service
public class CompensateCloseFacilityPaidOffCommandHandler
        extends WriteCommandHandler<CompensateCloseFacilityPaidOffCommand, Unit> {

    private static final Logger log = LoggerFactory.getLogger(CompensateCloseFacilityPaidOffCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final ApplicationNumberResolver applicationNumberResolver;
    private final Clock clock;

    public CompensateCloseFacilityPaidOffCommandHandler(
            WriteTransaction writeTransaction,
            TradeLoanFacilityRepository repository,
            InstallmentScheduleRepository installmentScheduleRepository,
            ApplicationNumberResolver applicationNumberResolver,
            Clock clock) {
        super(writeTransaction);
        this.repository = repository;
        this.installmentScheduleRepository = installmentScheduleRepository;
        this.applicationNumberResolver = applicationNumberResolver;
        this.clock = clock;
    }

    @Override
    protected Result<Unit> prepare(CompensateCloseFacilityPaidOffCommand command) {
        return Result.success();
    }

    @Override
    protected Result<List<DomainEvent<?>>> write(CompensateCloseFacilityPaidOffCommand command, Unit prepared) {
        List<DomainEvent<?>> allEvents = new ArrayList<>();

        return resolveIdentifiers(command)
                .flatMap(ids -> loadSchedule(ids)
                        .flatMap(schedule -> schedule.revertCollectInstallment(
                                        List.of(command.transactionReference()), clock)
                                .map(__ -> schedule))
                        .onSuccess(schedule -> allEvents.addAll(schedule.domainEvents()))
                        .onSuccess(installmentScheduleRepository::save)
                        .onSuccess(schedule -> log.info(
                                "Reverted installment schedule for compensate: applicationNumber={}, ref={}",
                                command.applicationNumber(),
                                command.transactionReference()))
                        .flatMap(__ -> loadFacility(ids))
                        .flatMap(facility -> facility.revertClosePaidOff(command.transactionReference(), clock)
                                .map(v -> facility))
                        .onSuccess(facility -> allEvents.addAll(facility.domainEvents()))
                        .onSuccess(facility -> {
                            repository.save(facility);
                            log.info(
                                    "Reverted close paid off for facility: applicationNumber={}, ref={}",
                                    command.applicationNumber(),
                                    command.transactionReference());
                        }))
                .map(__ -> allEvents);
    }

    private Result<ApplicationNumberResolver.LoanIdentifiers> resolveIdentifiers(
            CompensateCloseFacilityPaidOffCommand command) {
        return Result.fromOptional(
                applicationNumberResolver.resolveByApplicationNumber(command.applicationNumber()),
                () -> FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private Result<InstallmentSchedule> loadSchedule(ApplicationNumberResolver.LoanIdentifiers ids) {
        return Result.fromOptional(
                installmentScheduleRepository.findById(
                        InstallmentScheduleId.of(ids.installmentScheduleId()).unwrap()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        ids.installmentScheduleId())));
    }

    private Result<TradeLoanFacility> loadFacility(ApplicationNumberResolver.LoanIdentifiers ids) {
        return Result.fromOptional(
                repository.findById(LoanFacilityId.of(ids.loanFacilityId())),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, ids.loanFacilityId())));
    }
}
