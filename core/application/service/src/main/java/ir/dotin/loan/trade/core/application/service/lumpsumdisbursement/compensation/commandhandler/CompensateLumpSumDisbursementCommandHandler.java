package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.compensation.commandhandler;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.error.FailureCause;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateLumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
class CompensateLumpSumDisbursementCommandHandler implements CommandHandler<CompensateLumpSumDisbursementCommand> {

    private final TradeLoanFacilityRepository repository;
    private final TransactionPostingPort transactionPostingPort;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(CompensateLumpSumDisbursementCommand command) {
        log.warn("Compensating lump sum disbursement for facility: {}", command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> FailureCause.businessRule(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> facility.revertLumpSumDisbursement(clock)
                        .flatMap(transactionPostingPort::reverseTransactions)
                        .map(s -> facility))
                .onSuccess(repository::save)
                .map(TradeLoanFacility::domainEvents);
    }
}
