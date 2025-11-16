package ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.SubmitFacilityForApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.submitfacilityforapproval.i18n.SubmitFacilityForApprovalErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmitFacilityForApprovalCommandHandler implements CommandHandler<SubmitFacilityForApprovalCommand> {

    private static final Logger log = LoggerFactory.getLogger(SubmitFacilityForApprovalCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final TradeLoanFacilityService domainService;

    @Override
    public Result<List<DomainEvent<?>>> handle(SubmitFacilityForApprovalCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        return Result.fromOptional(
                        repository.findById(loanFacilityId),
                        () -> Notification.ofError(
                                SubmitFacilityForApprovalErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()))
                .flatMap(facility -> domainService.submitForApproval(facility).map(v -> facility))
                .peekValue(facility -> {
                    repository.save(facility);
                    log.info("Facility submitted for approval: {}", command.loanFacilityId());
                })
                .mapNonNull(TradeLoanFacility::domainEvents);
    }
}
