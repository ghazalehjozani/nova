package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.commandhandler;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.model.RetryPolicy;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularProgressiveDisbursementCommand;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step.ApplyDisbursementStep;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step.PostTransactionsStep;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step.ResolveAccountsStep;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step.ValidateFacilityStep;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularProgressiveDisbursementStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.read;
import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.remote;
import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public class IrregularProgressiveDisbursementCommandHandler
        implements WorkflowCommandHandler<IrregularProgressiveDisbursementCommand, IrregularDisbursementData> {

    private static final Logger log = LoggerFactory.getLogger(IrregularProgressiveDisbursementCommandHandler.class);

    @Override
    public Workflow<IrregularDisbursementData> definition() {
        // @formatter:off
        return Workflow.<IrregularDisbursementData>named("irregular-progressive-disbursement")
                .step(read(IrregularProgressiveDisbursementStep.VALIDATE_FACILITY, validateFacilityStep))
                .step(remote(IrregularProgressiveDisbursementStep.RESOLVE_ACCOUNTS, resolveAccountsStep)
                        .retry(RetryPolicy.CONSERVATIVE)
                        .timeout(Duration.ofSeconds(30)))
                .step(remote(IrregularProgressiveDisbursementStep.POST_TRANSACTIONS, postTransactionsStep)
                        .retry(RetryPolicy.CONSERVATIVE)
                        .timeout(Duration.ofSeconds(30)))
                .step(writePublishing(IrregularProgressiveDisbursementStep.APPLY_DISBURSEMENT, applyDisbursementStep))
                .build();
        // @formatter:on
    }

    @Override
    public Result<IrregularDisbursementData> seed(IrregularProgressiveDisbursementCommand command) {
        log.info("Starting irregular disbursement for facility: {}", command.loanFacilityId());

        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return dependencyLoader
                .loadFacility(loanFacilityId)
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility -> dependencyLoader
                        .loadInstallmentSchedule(facility)
                        .flatMap(schedule -> seedAssembler.approvePlan(command, facility, schedule)));
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.IRREGULAR_PROGRESSIVE)
                .map(ignored -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod() != null
                                        ? sl.getDisbursementMethod().name()
                                        : "null")
                                .orElse("UNKNOWN"))));
    }

    private final FacilityDependencyLoader dependencyLoader;
    private final BranchAccessValidator branchAccessValidator;
    private final IrregularDisbursementSeedAssembler seedAssembler;
    private final ValidateFacilityStep validateFacilityStep;
    private final ResolveAccountsStep resolveAccountsStep;
    private final PostTransactionsStep postTransactionsStep;
    private final ApplyDisbursementStep applyDisbursementStep;
}
