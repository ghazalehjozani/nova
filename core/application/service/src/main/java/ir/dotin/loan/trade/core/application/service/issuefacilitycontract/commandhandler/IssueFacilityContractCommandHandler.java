package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

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
import org.springframework.stereotype.Service;

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
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.definition.Steps;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.RetryPolicy;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractDependencyLoader;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractValidator;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.IssueFacilityContractStep;
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.FacilityContractValidation;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DocumentMetadataType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.IssueContractCommitmentHandlingStrategy;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.TradeIssueContractTransactionService;

@Service
public class IssueFacilityContractCommandHandler
        extends WorkflowCommandHandler<IssueFacilityContractCommand, ContractData> {

    private static final Logger log = LoggerFactory.getLogger(IssueFacilityContractCommandHandler.class);

    private final FacilityContractDependencyLoader dependencyLoader;
    private final FacilityContractValidator facilityValidator;
    private final FacilityContractValidation facilityContractValidation;
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

    private final Workflow<ContractData> workflow;

    public IssueFacilityContractCommandHandler(
            WorkflowEngine engine,
            FacilityContractDependencyLoader dependencyLoader,
            FacilityContractValidator facilityValidator,
            FacilityContractValidation facilityContractValidation,
            TradeLoanFacilityRepository facilityRepository,
            TradeLoanTypeRepository loanTypeRepository,
            TradeLoanArrangementRepository loanArrangementRepository,
            TradeIssueContractTransactionService transactionService,
            TransactionPostingPort transactionPostingPort,
            AccountResolutionService accountResolutionService,
            IssueFacilityContractConfiguration configuration,
            IssueContractCommitmentHandlingStrategy issueContractStrategy,
            LoanTopicResolver loanTopicResolver,
            Clock clock) {
        super(engine);
        this.dependencyLoader = dependencyLoader;
        this.facilityValidator = facilityValidator;
        this.facilityContractValidation = facilityContractValidation;
        this.facilityRepository = facilityRepository;
        this.loanTypeRepository = loanTypeRepository;
        this.loanArrangementRepository = loanArrangementRepository;
        this.transactionService = transactionService;
        this.transactionPostingPort = transactionPostingPort;
        this.accountResolutionService = accountResolutionService;
        this.configuration = configuration;
        this.issueContractStrategy = issueContractStrategy;
        this.loanTopicResolver = loanTopicResolver;
        this.clock = clock;
        this.workflow = buildWorkflow();
    }

    @Override
    protected Workflow<ContractData> workflow() {
        return workflow;
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

    private Workflow<ContractData> buildWorkflow() {
        return new Workflow<>() {
            @Override
            public String workflowType() {
                return "issue-facility-contract";
            }

            @Override
            public List<Step<ContractData>> steps() {
                return List.of(
                        Steps.read(
                                        IssueFacilityContractStep.VALIDATE_FACILITY,
                                        IssueFacilityContractCommandHandler.this::validateFacility)
                                .build(),
                        Steps.remote(
                                        IssueFacilityContractStep.OPEN_ACCOUNTS,
                                        IssueFacilityContractCommandHandler.this::openAccounts)
                                .retry(RetryPolicy.CONSERVATIVE)
                                .timeout(Duration.ofSeconds(30))
                                .compensatedBy(IssueFacilityContractCommandHandler.this::closeAccounts)
                                .build(),
                        Steps.remote(
                                        IssueFacilityContractStep.POST_TRANSACTION,
                                        IssueFacilityContractCommandHandler.this::postTransaction)
                                .retry(RetryPolicy.CONSERVATIVE)
                                .timeout(Duration.ofSeconds(30))
                                .compensatedBy(IssueFacilityContractCommandHandler.this::reverseTransaction)
                                .build(),
                        Steps.write(
                                        IssueFacilityContractStep.UPDATE_FACILITY_STATE,
                                        IssueFacilityContractCommandHandler.this::updateFacilityState)
                                .compensatedBy(IssueFacilityContractCommandHandler.this::revertFacilityState)
                                .build());
            }
        };
    }

    private StepResult<Void> validateFacility(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }

        var facility = facilityResult.unwrap();
        return StepResult.fromResult(facility.validateIssueContract());
    }

    private StepResult<Void> openAccounts(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();

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
            return StepResult.failure(result.err().orElseThrow());
        }

        ResolvedAccounts resolved = result.unwrap();
        Map<String, String> serializedAccounts = resolved.accountsByRelationType().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), e -> e.getValue().value()));

        ctx.updateData(d -> d.withResolvedAccounts(serializedAccounts));

        return new StepResult.Success<>(null);
    }

    private StepResult<Void> closeAccounts(WorkflowContext<ContractData> ctx) {
        Map<String, String> openedAccounts = ctx.data().resolvedAccounts();
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

    private StepResult<Void> postTransaction(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();
        ResolvedAccounts resolvedAccounts = data.getResolvedAccounts();

        var transactionResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanType(facility).flatMap(loanType -> createPostTitle(facility)
                        .flatMap(postTitle -> createTransaction(
                                facility, loanType, postTitle, data.transactionConfig(), resolvedAccounts))));

        if (transactionResult.isFailure()) {
            return StepResult.failure(transactionResult.err().orElseThrow());
        }

        var transaction = transactionResult.unwrap();
        var result = transactionPostingPort.postTransaction(transaction);

        if (result.isFailure()) {
            return StepResult.failure(result.err().orElseThrow());
        }

        var trackedNumber = result.unwrap();
        ctx.updateData(d -> d.withPostedTransaction(
                trackedNumber.value(), trackedNumber.trackingId(), trackedNumber.status(), trackedNumber.createdAt()));

        log.info("Transaction posted: {}", trackedNumber.value());
        return new StepResult.Success<>(null);
    }

    private StepResult<Void> reverseTransaction(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();
        log.warn("Reversing transaction: {}", data.postedTransactionNumber());

        var trackedNumber = TrackedTransactionNumber.create(
                Objects.requireNonNull(data.postedTransactionNumber(), "postedTransactionNumber"),
                Objects.requireNonNull(data.postedTrackingId(), "postedTrackingId"),
                TransactionStatus.POSTED,
                clock);

        return StepResult.fromResult(transactionPostingPort.reverseTransaction(trackedNumber));
    }

    private StepResult<List<DomainEvent<?>>> updateFacilityState(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();

        var result = loadFacility(LoanFacilityId.of(data.facilityId())).flatMap(facility -> {
            var trackedNumber = TrackedTransactionNumber.create(
                    Objects.requireNonNull(data.postedTransactionNumber(), "postedTransactionNumber"),
                    Objects.requireNonNull(data.postedTrackingId(), "postedTrackingId"),
                    Objects.requireNonNull(data.transactionStatus(), "transactionStatus"),
                    clock);

            return facility.issueContract(trackedNumber, data.getAccountIdsByRelationType(), clock)
                    .map(ignored -> {
                        List<DomainEvent<?>> events = List.copyOf(facility.domainEvents());
                        facilityRepository.save(facility, data.expectedVersion());
                        log.info("Contract issued: facilityId={}", data.facilityId());
                        return events;
                    });
        });

        return StepResult.fromWriteResult(result);
    }

    private StepResult<Void> revertFacilityState(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }

        var facility = facilityResult.unwrap();
        var revertResult = facility.revertContractIssuance(clock);

        if (revertResult.isFailure()) {
            return StepResult.fromResult(revertResult);
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
