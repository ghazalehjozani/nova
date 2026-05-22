package ir.dotin.loan.trade.core.application.service.collectinstallment.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.error.FailureCause;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentPaymentRecord;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand.InstallmentPaymentItem;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver.LoanIdentifiers;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollectInstallmentCommandHandler implements CommandHandler<CollectInstallmentCommand> {

    private static final Logger log = LoggerFactory.getLogger(CollectInstallmentCommandHandler.class);

    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final ApplicationNumberResolver applicationNumberResolver;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(CollectInstallmentCommand command) {
        return resolveIdentifiers(command)
                .flatMap(ids -> loadSchedule(ids, command))
                .flatMap(schedule -> collectAllPayments(schedule, command))
                .onSuccess(installmentScheduleRepository::save)
                .onSuccess(schedule -> log.info(
                        "Installment collection completed: applicationNumber={}, payments={}, ref={}",
                        command.applicationNumber(),
                        command.payments().size(),
                        command.transactionReference()))
                .map(AbstractAggregateRoot::domainEvents);
    }

    private Result<LoanIdentifiers> resolveIdentifiers(CollectInstallmentCommand command) {
        return Result.fromOptional(
                applicationNumberResolver.resolveByApplicationNumber(command.applicationNumber()),
                () -> FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private Result<InstallmentSchedule> loadSchedule(LoanIdentifiers ids, CollectInstallmentCommand command) {
        return Result.fromOptional(
                installmentScheduleRepository.findById(
                        InstallmentScheduleId.of(ids.installmentScheduleId()).unwrap()),
                () -> FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        ids.installmentScheduleId())));
    }

    private Result<InstallmentSchedule> collectAllPayments(
            InstallmentSchedule schedule, CollectInstallmentCommand command) {

        for (InstallmentPaymentItem item : command.payments()) {
            Result<InstallmentPaymentRecord> recordResult = buildPaymentRecord(item, command, schedule);
            if (recordResult.isFailure()) {
                return Result.failure(recordResult.err().orElseThrow());
            }

            Result<?> collectResult = schedule.collectInstallment(recordResult.unwrap(), clock);
            if (collectResult.isFailure()) {
                return Result.failure(collectResult.err().orElseThrow());
            }
        }

        return Result.success(schedule);
    }

    private Result<InstallmentPaymentRecord> buildPaymentRecord(
            InstallmentPaymentItem item, CollectInstallmentCommand command, InstallmentSchedule schedule) {

        CurrencyType currency = schedule.getCurrency();

        Result<Money> principalResult = Money.valueOf(item.principalAmount(), currency);
        if (principalResult.isFailure())
            return Result.failure(principalResult.err().orElseThrow());

        Result<Money> interestResult = Money.valueOf(item.interestAmount(), currency);
        if (interestResult.isFailure())
            return Result.failure(interestResult.err().orElseThrow());

        Result<Money> totalResult = Money.valueOf(item.totalPaidAmount(), currency);
        if (totalResult.isFailure()) return Result.failure(totalResult.err().orElseThrow());

        InstallmentPaymentRecord record = InstallmentPaymentRecord.builder()
                .paymentReference(command.transactionReference())
                .installmentSequenceNumber(item.installmentSequenceNumber())
                .principalAmount(principalResult.unwrap())
                .interestAmount(interestResult.unwrap())
                .totalPaidAmount(totalResult.unwrap())
                .valueDate(item.valueDate())
                .paymentDate(item.paymentDate())
                .channel(command.channel())
                .transactionReference(command.legacyTransactionReference())
                .build();

        Notification validation = record.validate();
        if (validation.hasErrors()) {
            return Result.failure(validation);
        }

        return Result.success(record);
    }
}
