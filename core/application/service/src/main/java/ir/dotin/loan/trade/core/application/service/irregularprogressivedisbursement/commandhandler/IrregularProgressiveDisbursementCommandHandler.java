package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.commandhandler;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
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

@Service
public class IrregularProgressiveDisbursementCommandHandler
        extends WorkflowCommandHandler<IrregularProgressiveDisbursementCommand, IrregularDisbursementData> {

    private static final Logger log = LoggerFactory.getLogger(IrregularProgressiveDisbursementCommandHandler.class);

    @Override
    protected Workflow<IrregularDisbursementData> route(WorkflowRoute<IrregularDisbursementData> route) {
        return route.type("irregular-progressive-disbursement")
                .read(IrregularProgressiveDisbursementStep.VALIDATE_FACILITY, validateFacilityStep)
                .remote(IrregularProgressiveDisbursementStep.RESOLVE_ACCOUNTS, resolveAccountsStep)
                .retry(RetryPolicy.CONSERVATIVE)
                .timeout(Duration.ofSeconds(30))
                .remote(IrregularProgressiveDisbursementStep.POST_TRANSACTIONS, postTransactionsStep)
                .retry(RetryPolicy.CONSERVATIVE)
                .timeout(Duration.ofSeconds(30))
                .write(IrregularProgressiveDisbursementStep.APPLY_DISBURSEMENT, applyDisbursementStep)
                .build();
    }

    @Override
    protected Result<IrregularDisbursementData> seed(IrregularProgressiveDisbursementCommand command) {
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

    public IrregularProgressiveDisbursementCommandHandler(
            WorkflowEngine engine,
            FacilityDependencyLoader dependencyLoader,
            BranchAccessValidator branchAccessValidator,
            IrregularDisbursementSeedAssembler seedAssembler,
            ValidateFacilityStep validateFacilityStep,
            ResolveAccountsStep resolveAccountsStep,
            PostTransactionsStep postTransactionsStep,
            ApplyDisbursementStep applyDisbursementStep) {
        super(engine);
        this.dependencyLoader = dependencyLoader;
        this.branchAccessValidator = branchAccessValidator;
        this.seedAssembler = seedAssembler;
        this.validateFacilityStep = validateFacilityStep;
        this.resolveAccountsStep = resolveAccountsStep;
        this.postTransactionsStep = postTransactionsStep;
        this.applyDisbursementStep = applyDisbursementStep;
    }
}
