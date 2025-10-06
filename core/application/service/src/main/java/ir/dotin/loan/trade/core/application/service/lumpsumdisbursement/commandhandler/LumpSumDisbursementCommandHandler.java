package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.driven.command.LumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.i18n.LumpSumDisbursementErrorCodes;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.mapper.LumpSumDisbursementCommandMapper;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LumpSumDisbursementCommandHandler implements CommandHandler<LumpSumDisbursementCommand> {

    private static final Logger log = LoggerFactory.getLogger(LumpSumDisbursementCommandHandler.class);

    private final LumpSumDisbursementCommandMapper mapper;
    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(LumpSumDisbursementCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> Notification.ofError(
                                LumpSumDisbursementErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()))
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility -> {
                    Money fullAmount = mapper.toMoney(command.fullAmount());
                    return facility.completeDisbursement(fullAmount, clock).map(ignored -> facility);
                })
                .peekValue(facility -> {
                    repository.save(facility);
                    log.info(
                            "Lump sum disbursement completed for facility: {}. Amount: {}",
                            command.loanFacilityId(),
                            command.fullAmount());
                })
                .mapNonNull(AbstractAggregateRoot::domainEvents);
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.LUMP_SUMP)
                .map(ignored -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        LumpSumDisbursementErrorCodes.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod().name())
                                .orElse("UNKNOWN"))));
    }
}
