package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.platform.pangaea.saga.api.handler.SagaCommandHandler;
import ir.dotin.platform.pangaea.saga.api.orchestration.SagaOrchestrator;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractDependencyLoader;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractValidator;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga.IssueFacilityContractInput;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga.IssueFacilityContractSagaData;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.FacilityContractValidation;

@Service
public class IssueFacilityContractCommandHandler
        extends SagaCommandHandler<IssueFacilityContractCommand, IssueFacilityContractSagaData> {

    private final FacilityContractDependencyLoader dependencyLoader;
    private final FacilityContractValidator facilityValidator;
    private final FacilityContractValidation facilityContractValidation;

    public IssueFacilityContractCommandHandler(
            SagaOrchestrator<IssueFacilityContractSagaData> sagaOrchestrator,
            FacilityContractDependencyLoader dependencyLoader,
            FacilityContractValidator facilityValidator,
            FacilityContractValidation facilityContractValidation) {
        super(sagaOrchestrator);
        this.dependencyLoader = dependencyLoader;
        this.facilityValidator = facilityValidator;
        this.facilityContractValidation = facilityContractValidation;
    }

    @Override
    protected String sagaType() {
        return "issue-facility-contract";
    }

    @Override
    protected Result<SagaInput> prepare(IssueFacilityContractCommand command) {
        return dependencyLoader.loadDependencies(command).flatMap(context -> facilityValidator
                .callAndValidateServices(command, context)
                .flatMap(ignored -> facilityContractValidation.validateForContractIssuance(
                        context.facility(), context.arrangement()))
                .map(ignored -> buildInput(command)));
    }

    private SagaInput buildInput(IssueFacilityContractCommand command) {
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

        return IssueFacilityContractInput.of(
                command.loanFacilityId(), command.branchCode(), transactionConfig, command.version());
    }
}
