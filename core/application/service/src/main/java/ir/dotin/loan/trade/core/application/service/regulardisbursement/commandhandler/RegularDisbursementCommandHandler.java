package ir.dotin.loan.trade.core.application.service.regulardisbursement.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RegularDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.regulardisbursement.mapper.RegularDisbursementCommandMapper;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegularDisbursementCommandHandler implements CommandHandler<RegularDisbursementCommand> {

    private static final Logger log = LoggerFactory.getLogger(RegularDisbursementCommandHandler.class);

    private final RegularDisbursementCommandMapper mapper;
    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(RegularDisbursementCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(this::validateDisbursementMethod)
                //                .flatMap(facility -> {
                //                    Money trancheAmount = mapper.toMoney(command.trancheAmount());
                //                    return facility.disbursement(trancheAmount, trackedNumbers, accountIds,
                // clock).map(ignored -> facility);
                //                })
                .onSuccess(facility -> {
                    repository.save(facility);
                    log.info(
                            "Regular disbursement tranche {} completed for facility: {}. Amount: {}, Total: {}",
                            command.trancheNumber(),
                            command.loanFacilityId(),
                            command.trancheAmount(),
                            facility.getTotalDisbursedAmount());
                })
                .map(AbstractAggregateRoot::domainEvents);
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.REGULAR_PROGRESSIVE)
                .map(sl -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod().name())
                                .orElse("UNKNOWN"))));
    }
}
