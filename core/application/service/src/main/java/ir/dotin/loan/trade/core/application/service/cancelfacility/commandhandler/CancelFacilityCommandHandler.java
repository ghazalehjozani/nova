package ir.dotin.loan.trade.core.application.service.cancelfacility.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.cancelfacility.i18n.CancelFacilityErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelFacilityCommandHandler implements CommandHandler<CancelFacilityCommand> {

    private static final Logger log = LoggerFactory.getLogger(CancelFacilityCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final TradeLoanFacilityService domainService;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(CancelFacilityCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> Notification.ofError(
                                CancelFacilityErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()))
                .flatMap(facility -> domainService
                        .cancel(facility, command.cancellationNotes())
                        .map(v -> facility))
                .peekValue(facility -> {
                    repository.save(facility);
                    log.debug("Facility cancelled: {}", command.loanFacilityId());
                })
                .mapNonNull(TradeLoanFacility::domainEvents);
    }
}
