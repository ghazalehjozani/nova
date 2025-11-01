package ir.dotin.loan.trade.core.application.service.approvefacility.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.approvefacility.factory.ApprovalStrategyFactory;
import ir.dotin.loan.trade.core.application.service.approvefacility.i18n.ApproveFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.approvefacility.strategy.ApprovalStrategy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApproveFacilityCommandHandler implements CommandHandler<ApproveFacilityCommand> {

    private static final Logger log = LoggerFactory.getLogger(ApproveFacilityCommandHandler.class);

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final ApprovalStrategyFactory strategyFactory;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(ApproveFacilityCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return Result.fromOptional(
                        loanFacilityRepository.findById(loanFacilityId),
                        () -> Notification.ofError(
                                ApproveFacilityErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()))
                .flatMap(facility -> Result.fromOptional(
                                loanArrangementRepository.findById(facility.getLoanArrangementId()),
                                () -> Notification.ofError(
                                        ApproveFacilityErrorCodes.LOAN_ARRANGEMENT_NOT_FOUND,
                                        facility.getLoanArrangementId()))
                        .flatMap(arrangement -> {
                            ApprovalStrategy strategy = strategyFactory.getStrategy(command);
                            return strategy.validate(command, facility, arrangement)
                                    .flatMap(ignored -> strategy.approve(facility, arrangement))
                                    .map(ignored -> {
                                        loanFacilityRepository.save(facility);
                                        log.debug("Facility approved: {}", command.loanFacilityId());
                                        return facility.domainEvents();
                                    });
                        }));
    }
}
