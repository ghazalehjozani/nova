package ir.dotin.loan.trade.core.application.service.irregulardisbursement.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.irregulardisbursement.i18n.IrregularDisbursementErrorCodes;
import ir.dotin.loan.trade.core.application.service.irregulardisbursement.mapper.IrregularDisbursementCommandMapper;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IrregularDisbursementCommandHandler implements CommandHandler<IrregularDisbursementCommand> {

    private static final Logger log = LoggerFactory.getLogger(IrregularDisbursementCommandHandler.class);

    private final IrregularDisbursementCommandMapper mapper;
    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(IrregularDisbursementCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> Notification.ofError(
                                IrregularDisbursementErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()))
                .flatMap(this::validateDisbursementMethod)
                //                .flatMap(facility -> {
                //                    Money requestedAmount = mapper.toMoney(command.requestedAmount());
                //                    // First request the disbursement
                //                    return facility.requestIrregularDisbursement(requestedAmount, clock)
                //                            .flatMap(ignored -> facility.disbursement(requestedAmount, trackedNumbers,
                // accountIds, clock))
                //                            .map(ignored -> facility);
                //                })
                .peekValue(facility -> {
                    repository.save(facility);
                    log.info(
                            "Irregular disbursement completed for facility: {}. Amount: {}, Total: {}",
                            command.loanFacilityId(),
                            command.requestedAmount(),
                            facility.getTotalDisbursedAmount());
                })
                .mapNonNull(AbstractAggregateRoot::domainEvents);
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.STAGED_IRREGULAR)
                .map(sl -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        IrregularDisbursementErrorCodes.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod().name())
                                .orElse("UNKNOWN"))));
    }
}
