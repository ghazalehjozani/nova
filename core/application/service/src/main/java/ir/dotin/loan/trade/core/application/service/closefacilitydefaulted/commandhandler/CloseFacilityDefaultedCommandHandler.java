package ir.dotin.loan.trade.core.application.service.closefacilitydefaulted.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteCommandHandler;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityDefaultedCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

@Service
public class CloseFacilityDefaultedCommandHandler extends WriteCommandHandler<CloseFacilityDefaultedCommand, Unit> {

    private static final Logger log = LoggerFactory.getLogger(CloseFacilityDefaultedCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final BranchAccessValidator branchAccessValidator;
    private final TradeLoanFacilityService domainService;

    public CloseFacilityDefaultedCommandHandler(
            WriteTransaction writeTransaction,
            TradeLoanFacilityRepository repository,
            BranchAccessValidator branchAccessValidator,
            TradeLoanFacilityService domainService) {
        super(writeTransaction);
        this.repository = repository;
        this.branchAccessValidator = branchAccessValidator;
        this.domainService = domainService;
    }

    @Override
    protected Result<Unit> prepare(CloseFacilityDefaultedCommand command) {
        return branchAccessValidator.verifyCallerCoversFacility(
                command.branchCode(), LoanFacilityId.of(command.loanFacilityId()));
    }

    @Override
    protected Result<List<DomainEvent<?>>> write(CloseFacilityDefaultedCommand command, Unit prepared) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> domainService.closeDefaulted(facility).map(v -> facility))
                .onSuccess(facility -> {
                    repository.save(facility, command.version());
                    log.debug("Facility closed as defaulted: {}", command.loanFacilityId());
                })
                .map(TradeLoanFacility::domainEvents);
    }
}
