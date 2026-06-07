package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.accounting.document.api.model.metadata.OperationalInfo;
import ir.dotin.platform.accounting.document.core.factory.DocumentMetadataFactory;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.saga.api.annotation.SagaHandler;
import ir.dotin.platform.pangaea.saga.api.context.SagaContext;
import ir.dotin.platform.pangaea.saga.api.definition.SagaDefinition;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.platform.pangaea.saga.api.definition.SagaStep;
import ir.dotin.platform.pangaea.saga.api.definition.SagaSteps;
import ir.dotin.platform.pangaea.saga.api.model.ResultStepAdapter;
import ir.dotin.platform.pangaea.saga.api.model.StepResult;
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
import ir.dotin.loan.trade.core.domain.shared.document.enums.DocumentMetadataType;
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
                contractInput.facilityId(),
                contractInput.branchCode(),
                contractInput.transactionConfig(),
                contractInput.expectedVersion());
    }

    private StepResult<Void> validateFacility(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.unwrap();
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

        if (result.isFailure()) {
            return new StepResult.Failure<>(result.err().orElseThrow());
        }

        ResolvedAccounts resolved = result.unwrap();
        Map<String, String> serializedAccounts = resolved.accountsByRelationType().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), e -> e.getValue().value()));

        ctx.updateSagaData(d -> d.withResolvedAccounts(serializedAccounts));

        return new StepResult.Success<>(serializedAccounts);
    }

    private StepResult<Void> closeAccounts(
            SagaContext<IssueFacilityContractSagaData> ctx, Map<String, String> openedAccounts) {
        if (openedAccounts == null || openedAccounts.isEmpty()) {
            return new StepResult.Success<>(null);
        }
        List<AccountNumber> accountNumbers = new ArrayList<>();
        for (String accountValue : openedAccounts.values()) {
            Result<AccountNumber> accountNumberResult = AccountNumber.of(accountValue);
            if (accountNumberResult.isFailure()) {
                log.warn("Skipping un-parsable account during compensation: {}", accountValue);
            } else {
                accountNumbers.add(accountNumberResult.unwrap());
            }
        }
        if (!accountNumbers.isEmpty()) {
            Result<List<AccountNumber>> closeResult = accountResolutionService.closeAccounts(accountNumbers);
            if (closeResult.isFailure()) {
                log.warn(
                        "Compensation close failed for {}: {}",
                        accountNumbers,
                        closeResult.err().orElseThrow());
            }
        }
        return new StepResult.Success<>(null);
    }

    private StepResult<String> postTransaction(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();
        ResolvedAccounts resolvedAccounts = data.getResolvedAccounts();

        var transactionResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanType(facility).flatMap(loanType -> createPostTitle(facility)
                        .flatMap(postTitle -> createTransaction(
                                facility, loanType, postTitle, data.transactionConfig(), resolvedAccounts))));

        if (transactionResult.isFailure()) {
            return new StepResult.Failure<>(transactionResult.err().orElseThrow());
        }

        var transaction = transactionResult.unwrap();
        var result = transactionPostingPort.postTransaction(transaction);

        if (result.isFailure()) {
            return new StepResult.Failure<>(result.err().orElseThrow());
        }

        var trackedNumber = result.unwrap();
        ctx.updateSagaData(d -> d.withPostedTransaction(
                trackedNumber.value(), trackedNumber.trackingId(), trackedNumber.status(), trackedNumber.createdAt()));

        log.info("Transaction posted: {}", trackedNumber.value());
        return new StepResult.Success<>(trackedNumber.value());
    }

    private StepResult<Void> reverseTransaction(
            SagaContext<IssueFacilityContractSagaData> ctx, String transactionNumber) {
        var data = ctx.getSagaData();
        log.warn("Reversing transaction: {}", transactionNumber);

        // postedTransactionNumber and postedTrackingId are guaranteed set by the postTransaction step before
        // compensation
        var trackedNumber = TrackedTransactionNumber.create(
                Objects.requireNonNull(data.postedTransactionNumber(), "postedTransactionNumber"),
                Objects.requireNonNull(data.postedTrackingId(), "postedTrackingId"),
                TransactionStatus.POSTED,
                clock);

        return ResultStepAdapter.toStepResultVoid(transactionPostingPort.reverseTransaction(trackedNumber));
    }

    private StepResult<Void> updateFacilityState(SagaContext<IssueFacilityContractSagaData> ctx) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.unwrap();

        // postedTransactionNumber, postedTrackingId, transactionStatus are guaranteed set by the postTransaction step
        var trackedNumber = TrackedTransactionNumber.create(
                Objects.requireNonNull(data.postedTransactionNumber(), "postedTransactionNumber"),
                Objects.requireNonNull(data.postedTrackingId(), "postedTrackingId"),
                Objects.requireNonNull(data.transactionStatus(), "transactionStatus"),
                clock);

        var issueResult = facility.issueContract(trackedNumber, data.getAccountIdsByRelationType(), clock);

        if (issueResult.isFailure()) {
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

        facilityRepository.save(facility, data.expectedVersion());

        log.info("Contract issued: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private StepResult<Void> revertFacilityState(SagaContext<IssueFacilityContractSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.unwrap();
        var revertResult = facility.revertContractIssuance(clock);

        if (revertResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted contract issuance: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<TradeLoanArrangement> loadLoanArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanArrangementRepository.findById(facility.getLoanArrangementId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                        facility.getLoanArrangementId(),
                        facility.getId().value())));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanTypeRepository.findById(facility.getLoanTypeId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value())));
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        return PostTitle.of(
                configuration.getPostTitleTemplate().formatted(facility.getId().value()));
    }

    private Result<LoanTransaction> createTransaction(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            PostTitle postTitle,
            TransactionConfig config,
            ResolvedAccounts resolvedAccounts) {

        return DocumentMetadataFactory.builder()
                .terminal(DocumentMetadataFactory.TerminalConfig.of(
                        Objects.requireNonNull(config.terminalType(), "terminalType"),
                        Objects.requireNonNull(config.terminalId(), "terminalId"),
                        Objects.requireNonNull(config.terminalIp(), "terminalIp")))
                .product(DocumentMetadataFactory.ProductConfig.of(
                        Objects.requireNonNull(config.productCode(), "productCode"),
                        loanType.getCode().value(),
                        facility.getLoanApplication()
                                .getApplicationNumber()
                                .get()
                                .formattedApplicationNumber()))
                .party(DocumentMetadataFactory.PartyConfig.of(
                        facility.getLoanApplication().getApplicant().customerNumber(),
                        facility.getLoanApplication().getApplicant().name().fullName(),
                        List.of()))
                .tool(DocumentMetadataFactory.ToolConfig.of(
                        Objects.requireNonNull(config.userId(), "userId"),
                        Objects.requireNonNull(config.toolSource(), "toolSource")))
                .network(DocumentMetadataFactory.NetworkConfig.of(
                        Objects.requireNonNull(config.networkType(), "networkType"),
                        Objects.requireNonNull(config.channel(), "channel")))
                .metadataType(DocumentMetadataType.ISSUE_CONTRACT.code())
                .operational(OperationalInfo.builder().build())
                .build()
                .flatMap(metadata -> transactionService.createIssueContractTransaction(
                        facility,
                        loanType,
                        BranchCode.of(Objects.requireNonNull(config.branchCode(), "branchCode"))
                                .unwrap(),
                        postTitle,
                        metadata,
                        resolvedAccounts));
    }
}
