package ir.dotin.loan.trade.core.application.service.closefacilitydefaulted.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityDefaultedCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloseFacilityDefaultedCommandHandler implements CommandHandler<CloseFacilityDefaultedCommand> {

    private static final Logger log = LoggerFactory.getLogger(CloseFacilityDefaultedCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final BranchAccessValidator branchAccessValidator;
    private final TradeLoanFacilityService domainService;

    @Override
    public Result<List<DomainEvent<?>>> handle(CloseFacilityDefaultedCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .flatMap(facility -> domainService.closeDefaulted(facility).map(v -> facility))
                .onSuccess(facility -> {
                    repository.save(facility, command.version());
                    log.debug("Facility closed as defaulted: {}", command.loanFacilityId());
                })
                .map(TradeLoanFacility::domainEvents);
    }
}
