package ir.dotin.loan.trade.core.application.service.approvefacility.compensation.commandhandler;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteCommandHandler;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
class CompensateApprovalCommandHandler extends WriteCommandHandler<CompensateApprovalCommand, Unit> {

    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    CompensateApprovalCommandHandler(
            WriteTransaction writeTransaction, TradeLoanFacilityRepository repository, Clock clock) {
        super(writeTransaction);
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    protected Result<Unit> prepare(CompensateApprovalCommand command) {
        log.warn("Compensating approval for facility: {}", command.loanFacilityId());
        return Result.success();
    }

    @Override
    protected Result<List<DomainEvent<?>>> write(CompensateApprovalCommand command, Unit prepared) {
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> facility.revertApproval(clock).map(v -> facility))
                .onSuccess(repository::save)
                .map(TradeLoanFacility::domainEvents);
    }
}
