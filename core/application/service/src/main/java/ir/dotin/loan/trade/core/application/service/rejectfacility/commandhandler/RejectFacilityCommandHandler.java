package ir.dotin.loan.trade.core.application.service.rejectfacility.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.RejectFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RejectFacilityCommandHandler implements CommandHandler<RejectFacilityCommand> {

    private static final Logger log = LoggerFactory.getLogger(RejectFacilityCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(RejectFacilityCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.uid()))
                .peekValue(facility -> {
                    facility.reject(clock);
                    repository.save(facility);
                    log.info("Facility rejected: {}", command.loanFacilityId());
                })
                .mapNonNull(TradeLoanFacility::domainEvents);
    }
}
