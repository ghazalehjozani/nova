package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.compensation.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteCommandHandler;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateLumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.account.FcbTransactionReverser;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
class CompensateLumpSumDisbursementCommandHandler
        extends WriteCommandHandler<
                CompensateLumpSumDisbursementCommand, CompensateLumpSumDisbursementCommandHandler.ReversalPreparation> {

    private final TradeLoanFacilityRepository repository;
    private final FcbTransactionReverser fcbTransactionReverser;
    private final Clock clock;

    CompensateLumpSumDisbursementCommandHandler(
            WriteTransaction writeTransaction,
            TradeLoanFacilityRepository repository,
            FcbTransactionReverser fcbTransactionReverser,
            Clock clock) {
        super(writeTransaction);
        this.repository = repository;
        this.fcbTransactionReverser = fcbTransactionReverser;
        this.clock = clock;
    }

    @Override
    protected Result<ReversalPreparation> prepare(CompensateLumpSumDisbursementCommand command) {
        log.warn("Compensating lump sum disbursement for facility: {}", command.loanFacilityId());
        return Result.success(new ReversalPreparation(new AtomicReference<>(new ArrayList<>())));
    }

    @Override
    protected Result<List<DomainEvent<?>>> write(
            CompensateLumpSumDisbursementCommand command, ReversalPreparation prepared) {
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> facility.revertLumpSumDisbursement(clock).map(transactionNumbers -> {
                    prepared.reversals().set(transactionNumbers);
                    return facility;
                }))
                .onSuccess(repository::save)
                .map(TradeLoanFacility::domainEvents);
    }

    @Override
    protected void afterCommit(
            CompensateLumpSumDisbursementCommand command, ReversalPreparation prepared, List<DomainEvent<?>> events) {
        List<TrackedTransactionNumber> reversed = prepared.reversals().get();
        if (reversed != null && !reversed.isEmpty()) {
            fcbTransactionReverser.reverseAllBestEffort(reversed);
        }
    }

    record ReversalPreparation(AtomicReference<List<TrackedTransactionNumber>> reversals) {}
}
