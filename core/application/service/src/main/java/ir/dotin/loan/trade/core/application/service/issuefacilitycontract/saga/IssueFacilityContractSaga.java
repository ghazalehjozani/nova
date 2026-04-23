package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.time.Clock;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.accounting.document.api.model.metadata.OperationalInfo;
import ir.dotin.platform.accounting.document.core.factory.DocumentMetadataFactory;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.saga.api.annotation.SagaHandler;
import ir.dotin.platform.saga.api.context.SagaContext;
import ir.dotin.platform.saga.api.definition.SagaDefinition;
import ir.dotin.platform.saga.api.definition.SagaInput;
import ir.dotin.platform.saga.api.definition.SagaStep;
import ir.dotin.platform.saga.api.definition.SagaSteps;
import ir.dotin.platform.saga.api.model.ResultStepAdapter;
import ir.dotin.platform.saga.api.model.StepError;
import ir.dotin.platform.saga.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityContractIssued;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.IssueContractCommitmentHandlingStrategy;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.TradeIssueContractTransactionService;

import lombok.RequiredArgsConstructor;

@SagaHandler
@Component
@RequiredArgsConstructor
public class IssueFacilityContractSaga implements SagaDefinition<IssueFacilityContractSagaData> {

    private static final Logger log = LoggerFactory.getLogger(IssueFacilityContractSaga.class);

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final TradeIssueContractTransactionService transactionService;
    private final TransactionPostingPort transactionPostingPort;
    private final AccountResolutionService accountResolutionService;
    private final IssueFacilityContractConfiguration configuration;
    private final IssueContractCommitmentHandlingStrategy issueContractStrategy;
    private final LoanTopicResolver loanTopicResolver;
    private final Clock clock;

    @Override
    public String sagaType() {
        return "issue-facility-contract";
    }

    @Override
    public boolean allowStrategyOverride() {
        return true;
    }

    @Override
    public boolean allowBreakpoints() {
        return true;
    }

    @Override
    public long timeoutMillis() {
        return SagaDefinition.super.timeoutMillis();
    }

    @Override
    public List<SagaStep<IssueFacilityContractSagaData, ?>> steps() {
        return List.of(
                SagaSteps.readOnlyStep(IssueFacilityContractStep.VALIDATE_FACILITY, this::validateFacility)
                        .withNoRetry(),
                SagaSteps.step(IssueFacilityContractStep.OPEN_ACCOUNTS, this::openAccounts, this::closeAccounts)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(30)),
                SagaSteps.step(
                                IssueFacilityContractStep.POST_TRANSACTION,
                                this::postTransaction,
                                this::reverseTransaction)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(30)),
                SagaSteps.step(
                                IssueFacilityContractStep.UPDATE_FACILITY_STATE,
                                this::updateFacilityState,
                                this::revertFacilityState)
                        .withNoRetry());
    }

    @Override
    public IssueFacilityContractSagaData createInitialData(SagaInput input) {
        var contractInput = (IssueFacilityContractInput) input;
        return IssueFacilityContractSagaData.initial(
                contractInput.facilityId(), contractInput.branchCode(), contractInput.transactionConfig());
    }

    private StepResult<Void> validateFacility(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.hasErrors()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.orElseThrow();
        var validationResult = facility.validateIssueContract();

        return ResultStepAdapter.toStepResultVoid(validationResult);
    }

    private StepResult<Map<String, String>> openAccounts(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();

        var result = loadFacility(LoanFacilityId.of(data.facilityId())).flatMap(facility -> loadLoanType(facility)
                .flatMap(loanType -> loadLoanArrangement(facility).flatMap(arrangement -> {
                    Set<TradeRelationType> requiredRelationTypes =
                            new HashSet<>(issueContractStrategy.getRequiredRelationTypes());

                    Set<LoanTopic> requiredTopics = loanTopicResolver.resolveTopics(
                            loanType, facility.getLoanApplication().getEconomicSector(), requiredRelationTypes);

                    return accountResolutionService.resolveAccounts(
                            requiredTopics,
                            facility.getAccountInfoMap(),
                            arrangement.getCurrencyType().getCode());
                })));

        if (result.hasErrors()) {
            return new StepResult.Failure<>(new StepError.BusinessRuleError(result.notification()));
        }

        ResolvedAccounts resolved = result.orElseThrow();
        Map<String, String> serializedAccounts = resolved.accountsByRelationType().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), e -> e.getValue().value()));

        ctx.updateSagaData(d -> d.withResolvedAccounts(serializedAccounts));

        return new StepResult.Success<>(serializedAccounts);
    }

    private StepResult<Void> closeAccounts(
            SagaContext<IssueFacilityContractSagaData> ctx, Map<String, String> openedAccounts) {
        // TODO:
        log.warn("Compensating opened accounts: {}", openedAccounts);
        return new StepResult.Success<>(null);
    }

    private StepResult<String> postTransaction(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();
        ResolvedAccounts resolvedAccounts = data.getResolvedAccounts();

        var transactionResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanType(facility).flatMap(loanType -> createPostTitle(facility)
                        .flatMap(postTitle -> createTransaction(
                                facility, loanType, postTitle, data.transactionConfig(), resolvedAccounts))));

        if (transactionResult.hasErrors()) {
            return new StepResult.Failure<>(new StepError.BusinessRuleError(transactionResult.notification()));
        }

        var transaction = transactionResult.orElseThrow();
        var result = transactionPostingPort.postTransaction(transaction);

        if (result.hasErrors()) {
            return new StepResult.Failure<>(new StepError.BusinessRuleError(result.notification()));
        }

        var trackedNumber = result.orElseThrow();
        ctx.updateSagaData(d -> d.withPostedTransaction(
                trackedNumber.value(), trackedNumber.trackingId(), trackedNumber.status(), trackedNumber.createdAt()));

        log.info("Transaction posted: {}", trackedNumber.value());
        return new StepResult.Success<>(trackedNumber.value());
    }

    private StepResult<Void> reverseTransaction(
            SagaContext<IssueFacilityContractSagaData> ctx, String transactionNumber) {
        var data = ctx.getSagaData();
        log.warn("Reversing transaction: {}", transactionNumber);

        var trackedNumber = TrackedTransactionNumber.create(
                data.postedTransactionNumber(), data.postedTrackingId(), TransactionStatus.POSTED, clock);

        return ResultStepAdapter.toStepResultVoid(transactionPostingPort.reverseTransaction(trackedNumber));
    }

    private StepResult<Void> updateFacilityState(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.hasErrors()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.orElseThrow();

        var trackedNumber = TrackedTransactionNumber.create(
                data.postedTransactionNumber(), data.postedTrackingId(), data.transactionStatus(), clock);

        var issueResult = facility.issueContract(trackedNumber, data.getAccountIdsByRelationType(), clock);

        if (issueResult.hasErrors()) {
            return ResultStepAdapter.toStepResultVoid(issueResult);
        }

        var capturedEvents = facility.domainEvents().stream()
                .filter(TradeLoanFacilityContractIssued.class::isInstance)
                .map(TradeLoanFacilityContractIssued.class::cast)
                .map(event -> IssueFacilityContractSagaData.CapturedEventData.contractIssued(
                        event.eventType(),
                        event.aggregateId(),
                        event.sanctionedLoanId(),
                        event.transactionNumber(),
                        event.createdAt()))
                .toList();

        ctx.updateSagaData(d -> d.withCapturedEvents(capturedEvents));

        facilityRepository.save(facility);

        log.info("Contract issued: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private StepResult<Void> revertFacilityState(SagaContext<IssueFacilityContractSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.hasErrors()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.orElseThrow();
        var revertResult = facility.revertContractIssuance(clock);

        if (revertResult.hasErrors()) {
            return ResultStepAdapter.toStepResultVoid(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted contract issuance: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId facilityId) {
        return Result.fromOptional(
                facilityRepository.findById(facilityId),
                Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, facilityId));
    }

    private Result<TradeLoanArrangement> loadLoanArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanArrangementRepository.findById(facility.getLoanArrangementId()),
                Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND, facility.getLoanArrangementId()));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanTypeRepository.findById(facility.getLoanTypeId()),
                Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value()));
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        return PostTitle.of(
                configuration.postTitleTemplate().formatted(facility.getId().value()));
    }

    private Result<LoanTransaction> createTransaction(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            PostTitle postTitle,
            TransactionConfig config,
            ResolvedAccounts resolvedAccounts) {

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
                        facility.getLoanApplication().getApplicant().customerNumber(),
                        facility.getLoanApplication().getApplicant().name().fullName(),
                        List.of()))
                .tool(DocumentMetadataFactory.ToolConfig.of(config.userId(), config.toolSource()))
                .network(DocumentMetadataFactory.NetworkConfig.of(config.networkType(), config.channel()))
                .operational(OperationalInfo.builder().build())
                .build()
                .flatMap(metadata -> transactionService.createIssueContractTransaction(
                        facility,
                        loanType,
                        BranchCode.of(config.branchCode()).getValue(),
                        postTitle,
                        metadata,
                        resolvedAccounts));
    }
}
