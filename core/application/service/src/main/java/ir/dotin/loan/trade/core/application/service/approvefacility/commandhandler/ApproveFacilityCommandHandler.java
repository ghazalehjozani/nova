package ir.dotin.loan.trade.core.application.service.approvefacility.commandhandler;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteCommandHandler;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.approvefacility.component.SanctionDetailsLoader;
import ir.dotin.loan.trade.core.application.service.approvefacility.factory.ApprovalStrategyFactory;
import ir.dotin.loan.trade.core.application.service.approvefacility.strategy.ApprovalStrategy;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

@Service
public class ApproveFacilityCommandHandler
        extends WriteCommandHandler<ApproveFacilityCommand, ApproveFacilityCommandHandler.ApprovalPreparation> {

    private static final Logger log = LoggerFactory.getLogger(ApproveFacilityCommandHandler.class);

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final BranchAccessValidator branchAccessValidator;
    private final ApprovalStrategyFactory strategyFactory;
    private final SanctionDetailsLoader sanctionDetailsLoader;

    public ApproveFacilityCommandHandler(
            WriteTransaction writeTransaction,
            TradeLoanFacilityRepository loanFacilityRepository,
            TradeLoanArrangementRepository loanArrangementRepository,
            BranchAccessValidator branchAccessValidator,
            ApprovalStrategyFactory strategyFactory,
            SanctionDetailsLoader sanctionDetailsLoader) {
        super(writeTransaction);
        this.loanFacilityRepository = loanFacilityRepository;
        this.loanArrangementRepository = loanArrangementRepository;
        this.branchAccessValidator = branchAccessValidator;
        this.strategyFactory = strategyFactory;
        this.sanctionDetailsLoader = sanctionDetailsLoader;
    }

    @Override
    protected Result<ApprovalPreparation> prepare(ApproveFacilityCommand command) {
        ConfirmType confirmType = ConfirmType.of(command.confirmType()).unwrap();

        if (command.sanctionSerial() == null) {
            return Result.success(new ApprovalPreparation(confirmType, null));
        }

        return sanctionDetailsLoader
                .loadForManualApproval(command.loanFacilityId())
                .map(details -> new ApprovalPreparation(confirmType, details));
    }

    @Override
    protected Result<List<DomainEvent<?>>> write(ApproveFacilityCommand command, ApprovalPreparation prepared) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return Result.fromOptional(
                        loanFacilityRepository.findById(loanFacilityId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .flatMap(facility -> Result.fromOptional(
                                loanArrangementRepository.findById(facility.getLoanArrangementId()),
                                () -> FailureCause.notFound(Notification.ofError(
                                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                                        facility.getLoanArrangementId())))
                        .flatMap(arrangement -> {
                            ApprovalStrategy strategy = strategyFactory.getStrategy(command);
                            return strategy.validate(command, facility, arrangement)
                                    .flatMap(ignored -> strategy.approve(
                                            command,
                                            facility,
                                            arrangement,
                                            prepared.confirmType(),
                                            prepared.sanctionDetails()))
                                    .map(ignored -> {
                                        loanFacilityRepository.save(facility, command.version());
                                        log.debug("Facility approved: {}", command.loanFacilityId());
                                        return facility.domainEvents();
                                    });
                        }));
    }

    public record ApprovalPreparation(
            ConfirmType confirmType, @Nullable SanctionDetails sanctionDetails) {}
}
