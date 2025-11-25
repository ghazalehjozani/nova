package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import ir.dotin.platform.saga.api.model.StepError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.saga.api.annotation.SagaHandler;
import ir.dotin.platform.saga.api.context.SagaContext;
import ir.dotin.platform.saga.api.definition.SagaDefinition;
import ir.dotin.platform.saga.api.definition.SagaInput;
import ir.dotin.platform.saga.api.definition.SagaStep;
import ir.dotin.platform.saga.api.definition.SagaSteps;
import ir.dotin.platform.saga.api.model.ResultStepAdapter;
import ir.dotin.platform.saga.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.factory.DocumentMetadataFactory;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.OperationalInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.i18n.IssueFacilityContractErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.transaction.TradeIssueContractTransactionService;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.RequiredArgsConstructor;

@SagaHandler
@Component
@RequiredArgsConstructor
public class IssueFacilityContractSaga implements SagaDefinition<IssueFacilityContractSagaData> {

    private static final Logger log = LoggerFactory.getLogger(IssueFacilityContractSaga.class);

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeIssueContractTransactionService transactionService;
    private final TransactionPostingPort transactionPostingPort;
    private final IssueFacilityContractConfiguration configuration;
    private final Clock clock;

    @Override
    public String sagaType() {
        return "issue-facility-contract";
    }

    @Override
    public List<SagaStep<IssueFacilityContractSagaData, ?>> steps() {
        return List.of(
                SagaSteps.readOnlyStep(IssueFacilityContractStep.VALIDATE_FACILITY, this::validateFacility)
                        .noRetry(),
                SagaSteps.readOnlyStep(IssueFacilityContractStep.PREPARE_TRANSACTION, this::prepareTransaction)
                        .noRetry(),
                SagaSteps.step(
                                IssueFacilityContractStep.POST_TRANSACTION,
                                this::postTransaction,
                                this::reverseTransaction)
                        .conservativeRetry()
                        .withTimeout(Duration.ofSeconds(60)),
                SagaSteps.step(
                                IssueFacilityContractStep.UPDATE_FACILITY_STATE,
                                this::updateFacilityState,
                                this::revertFacilityState)
                        .noRetry());
    }

    @Override
    public IssueFacilityContractSagaData createInitialData(SagaInput input) {
        var contractInput = (IssueFacilityContractInput) input;
        return IssueFacilityContractSagaData.initial(
                contractInput.facilityId(), contractInput.branchCode(), contractInput.transactionConfig());
    }

    private StepResult<Void> validateFacility(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();
        return ResultStepAdapter.toStepResultVoid(loadFacility(data.facilityId()));
    }

    private StepResult<LoanTransaction> prepareTransaction(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();

        var result = loadFacility(data.facilityId())
                .flatMap(facility -> loadLoanType(facility)
                        .flatMap(loanType -> createPostTitle(facility)
                                .flatMap(postTitle -> createTransaction(
                                        facility, loanType, postTitle, data.transactionConfig()))));

        if (result.hasErrors()) {
            return new StepResult.Failure<>(
                    new StepError.BusinessRuleError(result.notification()));
        }

        var transaction = result.orElseThrow();
        var accountIds = transaction.extractAccountIdsByRelationType();
        ctx.updateSagaData(d -> d.withPreparedTransaction(transaction, accountIds));

        return new StepResult.Success<>(transaction);
    }

    private StepResult<TrackedTransactionNumber> postTransaction(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();

        var result = transactionPostingPort.postTransaction(data.preparedTransaction());

        if (result.hasErrors()) {
            return new StepResult.Failure<>(
                    new StepError.BusinessRuleError(result.notification()));
        }

        var transactionNumber = result.orElseThrow();
        ctx.updateSagaData(d -> d.withPostedTransaction(transactionNumber));

        log.info("Transaction posted: {}", transactionNumber);
        return new StepResult.Success<>(transactionNumber);
    }

    private StepResult<Void> reverseTransaction(
            SagaContext<IssueFacilityContractSagaData> ctx, TrackedTransactionNumber transactionNumber) {
        log.warn("Reversing transaction: {}", transactionNumber);
        return ResultStepAdapter.toStepResultVoid(
                transactionPostingPort.reverseTransactions(transactionNumber));
    }

    private StepResult<Void> updateFacilityState(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(data.facilityId());
        if (facilityResult.hasErrors()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.orElseThrow();
        facility.issueContract(data.postedTransactionNumber(), data.getAccountIdsByRelationType(), clock);
        facilityRepository.save(facility);

        log.info("Contract issued: {}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private StepResult<Void> revertFacilityState(SagaContext<IssueFacilityContractSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(data.facilityId());
        if (facilityResult.hasErrors()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.orElseThrow();
        var revertResult = facility.revertContractIssuance(clock);

        if (revertResult.hasErrors()) {
            return ResultStepAdapter.toStepResultVoid(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted contract issuance: {}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId facilityId) {
        return Result.fromOptional(
                facilityRepository.findById(facilityId),
                Notification.ofError(IssueFacilityContractErrorCodes.FACILITY_NOT_FOUND, facilityId));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanTypeRepository.findById(facility.getLoanTypeId()),
                Notification.ofError(
                        IssueFacilityContractErrorCodes.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value()));
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        return PostTitle.of(
                configuration.postTitleTemplate().formatted(facility.getId().value()));
    }

    private Result<LoanTransaction> createTransaction(
            TradeLoanFacility facility, TradeLoanType loanType, PostTitle postTitle, TransactionConfig config) {

        return DocumentMetadataFactory.builder()
                .terminal(DocumentMetadataFactory.TerminalConfig.of(
                        config.terminalType(), config.terminalId(), config.terminalIp()))
                .product(DocumentMetadataFactory.ProductConfig.of(
                        config.productCode(),
                        loanType.getCode().value(),
                        facility.getLoanApplication()
                                .getApplicationNumber()
                                .get()
                                .formattedApplicationNumber()))
                .party(DocumentMetadataFactory.PartyConfig.of(
                        facility.getLoanApplication().getCustomer().customerNumber(),
                        facility.getLoanApplication().getCustomer().name().fullName(),
                        List.of()))
                .tool(DocumentMetadataFactory.ToolConfig.of(config.userId(), config.toolSource()))
                .network(DocumentMetadataFactory.NetworkConfig.of(config.networkType(), config.channel()))
                .operational(OperationalInfo.builder().build())
                .build()
                .flatMap(metadata -> transactionService.createIssueContractTransaction(
                        facility, loanType, BranchCode.of(config.branchCode()).getValue(), postTitle, metadata));
    }
}
