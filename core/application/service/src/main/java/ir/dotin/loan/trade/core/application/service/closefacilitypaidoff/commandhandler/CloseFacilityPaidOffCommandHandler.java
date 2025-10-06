package ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.driven.command.CloseFacilityPaidOffCommand;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.closefacilitypaidoff.i18n.CloseFacilityPaidOffErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloseFacilityPaidOffCommandHandler implements CommandHandler<CloseFacilityPaidOffCommand> {

    private static final Logger log = LoggerFactory.getLogger(CloseFacilityPaidOffCommandHandler.class);

    private final TradeLoanFacilityService domainService;
    private final TradeLoanFacilityRepository repository;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(CloseFacilityPaidOffCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> Notification.ofError(
                                CloseFacilityPaidOffErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()))
                .peekValue(facility -> {
                    domainService.closePaidOff(facility);
                    repository.save(facility);
                    log.debug("Facility closed as paid off: {}", command.loanFacilityId());
                })
                .mapNonNull(TradeLoanFacility::domainEvents);
    }
}
