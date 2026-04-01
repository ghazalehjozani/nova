package ir.dotin.loan.trade.core.application.service.approvefacility.compensation.commandhandler;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
class CompensateApprovalCommandHandler implements CommandHandler<CompensateApprovalCommand> {

    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(CompensateApprovalCommand command) {
        log.warn("Compensating approval for facility: {}", command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId()))
                .flatMap(facility -> facility.revertApproval(clock).map(v -> facility))
                .peekValue(repository::save)
                .mapNonNull(TradeLoanFacility::domainEvents);
    }
}
