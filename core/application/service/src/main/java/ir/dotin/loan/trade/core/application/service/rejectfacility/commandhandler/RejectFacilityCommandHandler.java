package ir.dotin.loan.trade.core.application.service.rejectfacility.commandhandler;

import java.time.Clock;
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
import ir.dotin.loan.trade.core.application.ports.inbound.command.RejectFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Service
public class RejectFacilityCommandHandler extends WriteCommandHandler<RejectFacilityCommand, Unit> {

    private static final Logger log = LoggerFactory.getLogger(RejectFacilityCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final BranchAccessValidator branchAccessValidator;
    private final Clock clock;

    public RejectFacilityCommandHandler(
            WriteTransaction writeTransaction,
            TradeLoanFacilityRepository repository,
            BranchAccessValidator branchAccessValidator,
            Clock clock) {
        super(writeTransaction);
        this.repository = repository;
        this.branchAccessValidator = branchAccessValidator;
        this.clock = clock;
    }

    @Override
    protected Result<Unit> prepare(RejectFacilityCommand command) {
        return Result.success();
    }

    @Override
    protected Result<List<DomainEvent<?>>> write(RejectFacilityCommand command, Unit prepared) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.uid())))
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .onSuccess(facility -> {
                    facility.reject(clock);
                    repository.save(facility, command.version());
                    log.info("Facility rejected: {}", command.loanFacilityId());
                })
                .map(TradeLoanFacility::domainEvents);
    }
}
