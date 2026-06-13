package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.commandhandler;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.model.RetryPolicy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step.ApplyDisbursementStep;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step.PostTransactionsStep;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step.ResolveAccountsStep;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step.ValidateFacilityStep;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumDisbursementStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.read;
import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.remote;
import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public class LumpSumDisbursementCommandHandler
        implements WorkflowCommandHandler<LumpSumDisbursementCommand, LumpSumData> {

    private static final Logger log = LoggerFactory.getLogger(LumpSumDisbursementCommandHandler.class);

    @Override
    public Workflow<LumpSumData> definition() {
        // @formatter:off
        return Workflow.<LumpSumData>named("lump-sum-disbursement")
                .step(read(LumpSumDisbursementStep.VALIDATE_FACILITY, validateFacilityStep))
                .step(remote(LumpSumDisbursementStep.RESOLVE_ACCOUNTS, resolveAccountsStep)
                        .retry(RetryPolicy.CONSERVATIVE)
                        .timeout(Duration.ofSeconds(30)))
                .step(remote(LumpSumDisbursementStep.POST_TRANSACTIONS, postTransactionsStep)
                        .retry(RetryPolicy.CONSERVATIVE)
                        .timeout(Duration.ofSeconds(30)))
                .step(writePublishing(LumpSumDisbursementStep.APPLY_DISBURSEMENT, applyDisbursementStep))
                .build();
        // @formatter:on
    }

    @Override
    public Result<LumpSumData> seed(LumpSumDisbursementCommand command) {
        log.info("Starting lump sum disbursement for facility: {}", command.loanFacilityId());

        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return dependencyLoader
                .loadFacility(loanFacilityId)
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .flatMap(seedAssembler::validateDisbursementMethod)
                .map(ignored -> seedAssembler.buildData(command));
    }

    private final FacilityDependencyLoader dependencyLoader;
    private final BranchAccessValidator branchAccessValidator;
    private final LumpSumDisbursementSeedAssembler seedAssembler;
    private final ValidateFacilityStep validateFacilityStep;
    private final ResolveAccountsStep resolveAccountsStep;
    private final PostTransactionsStep postTransactionsStep;
    private final ApplyDisbursementStep applyDisbursementStep;
}
