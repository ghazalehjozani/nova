package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import java.time.Duration;

import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.RetryPolicy;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractDependencyLoader;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractValidator;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step.OpenAccountsStep;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step.PostTransactionStep;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step.UpdateFacilityStateStep;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step.ValidateFacilityStep;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.IssueFacilityContractStep;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.FacilityContractValidation;

@Service
public class IssueFacilityContractCommandHandler
        extends WorkflowCommandHandler<IssueFacilityContractCommand, ContractData> {

    @Override
    protected Workflow<ContractData> route(WorkflowRoute<ContractData> route) {
        // @formatter:off
        return route.type("issue-facility-contract")
                .read(IssueFacilityContractStep.VALIDATE_FACILITY, validateFacilityStep)
                .remote(IssueFacilityContractStep.OPEN_ACCOUNTS, openAccountsStep)
                    .retry(RetryPolicy.CONSERVATIVE)
                    .timeout(Duration.ofSeconds(30))
                .remote(IssueFacilityContractStep.POST_TRANSACTION, postTransactionStep)
                    .retry(RetryPolicy.CONSERVATIVE)
                    .timeout(Duration.ofSeconds(30))
                .write(IssueFacilityContractStep.UPDATE_FACILITY_STATE, updateFacilityStateStep)
                .build();
        // @formatter:on
    }

    @Override
    protected Result<ContractData> seed(IssueFacilityContractCommand command) {
        return dependencyLoader.loadDependencies(command).flatMap(context -> facilityValidator
                .callAndValidateServices(command, context)
                .flatMap(ignored -> facilityContractValidation.validateForContractIssuance(
                        context.facility(), context.arrangement()))
                .map(ignored -> buildData(command)));
    }

    private ContractData buildData(IssueFacilityContractCommand command) {
        TransactionConfig transactionConfig = TransactionConfig.builder()
                .userId(command.userId())
                .branchCode(command.branchCode())
                .terminalId(DocumentMetadataUtils.orEmpty(command.terminalId()))
                .terminalIp(DocumentMetadataUtils.orEmpty(command.terminalIp()))
                .terminalType(DocumentMetadataUtils.orEmpty(command.terminalType()))
                .channel(DocumentMetadataUtils.orEmpty(command.channel()))
                .toolSource(DocumentMetadataUtils.orEmpty(command.toolSource()))
                .productCode(DocumentMetadataUtils.orEmpty(command.productCode()))
                .networkType(DocumentMetadataUtils.orEmpty(command.networkType()))
                .build();

        return ContractData.initial(
                command.loanFacilityId(), command.branchCode(), transactionConfig, command.version());
    }

    private final FacilityContractDependencyLoader dependencyLoader;
    private final FacilityContractValidator facilityValidator;
    private final FacilityContractValidation facilityContractValidation;
    private final ValidateFacilityStep validateFacilityStep;
    private final OpenAccountsStep openAccountsStep;
    private final PostTransactionStep postTransactionStep;
    private final UpdateFacilityStateStep updateFacilityStateStep;

    public IssueFacilityContractCommandHandler(
            WorkflowEngine engine,
            FacilityContractDependencyLoader dependencyLoader,
            FacilityContractValidator facilityValidator,
            FacilityContractValidation facilityContractValidation,
            ValidateFacilityStep validateFacilityStep,
            OpenAccountsStep openAccountsStep,
            PostTransactionStep postTransactionStep,
            UpdateFacilityStateStep updateFacilityStateStep) {
        super(engine);
        this.dependencyLoader = dependencyLoader;
        this.facilityValidator = facilityValidator;
        this.facilityContractValidation = facilityContractValidation;
        this.validateFacilityStep = validateFacilityStep;
        this.openAccountsStep = openAccountsStep;
        this.postTransactionStep = postTransactionStep;
        this.updateFacilityStateStep = updateFacilityStateStep;
    }
}
