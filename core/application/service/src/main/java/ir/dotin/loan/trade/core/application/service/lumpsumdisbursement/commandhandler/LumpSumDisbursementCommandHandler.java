package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.commandhandler;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
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
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.configuration.LumpSumDisbursementConfiguration;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumDisbursementStep;
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DocumentMetadataType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.TradeLumpSumDisbursementTransactionService;

@Service
public class LumpSumDisbursementCommandHandler
        extends WorkflowCommandHandler<LumpSumDisbursementCommand, LumpSumData> {

    private static final Logger log = LoggerFactory.getLogger(LumpSumDisbursementCommandHandler.class);

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final TradeLumpSumDisbursementTransactionService transactionService;
    private final TransactionPostingPort transactionPostingPort;
    private final LumpSumDisbursementConfiguration configuration;
    private final DisbursementStrategyProvider strategyProvider;
    private final LoanTopicResolver loanTopicResolver;
    private final AccountResolutionService accountResolutionService;
    private final BranchAccessValidator branchAccessValidator;
    private final Clock clock;

    private final Workflow<LumpSumData> workflow;

    public LumpSumDisbursementCommandHandler(
            WorkflowEngine engine,
            TradeLoanFacilityRepository facilityRepository,
            TradeLoanTypeRepository loanTypeRepository,
            TradeLoanArrangementRepository loanArrangementRepository,
            InstallmentScheduleRepository installmentScheduleRepository,
            TradeLumpSumDisbursementTransactionService transactionService,
            TransactionPostingPort transactionPostingPort,
            LumpSumDisbursementConfiguration configuration,
            DisbursementStrategyProvider strategyProvider,
            LoanTopicResolver loanTopicResolver,
            AccountResolutionService accountResolutionService,
            BranchAccessValidator branchAccessValidator,
            Clock clock) {
        super(engine);
        this.facilityRepository = facilityRepository;
        this.loanTypeRepository = loanTypeRepository;
        this.loanArrangementRepository = loanArrangementRepository;
        this.installmentScheduleRepository = installmentScheduleRepository;
        this.transactionService = transactionService;
        this.transactionPostingPort = transactionPostingPort;
        this.configuration = configuration;
        this.strategyProvider = strategyProvider;
        this.loanTopicResolver = loanTopicResolver;
        this.accountResolutionService = accountResolutionService;
        this.branchAccessValidator = branchAccessValidator;
        this.clock = clock;
        this.workflow = buildWorkflow();
    }

    @Override
    protected Workflow<LumpSumData> workflow() {
        return workflow;
    }

    @Override
    protected Result<LumpSumData> seed(LumpSumDisbursementCommand command) {
        log.info("Starting lump sum disbursement for facility: {}", command.loanFacilityId());

        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return loadFacility(loanFacilityId)
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .flatMap(this::validateDisbursementMethod)
                .map(ignored -> buildData(command));
    }

    private LumpSumData buildData(LumpSumDisbursementCommand command) {
        TransactionConfig transactionConfig = new TransactionConfig(
                DocumentMetadataUtils.orEmpty(command.terminalType()),
                DocumentMetadataUtils.orEmpty(command.terminalId()),
                DocumentMetadataUtils.orEmpty(command.terminalIp()),
                DocumentMetadataUtils.orEmpty(command.productCode()),
                command.userId(),
                DocumentMetadataUtils.orEmpty(command.toolSource()),
                DocumentMetadataUtils.orEmpty(command.networkType()),
                command.branchCode(),
                DocumentMetadataUtils.orEmpty(command.channel()));

        return LumpSumData.initial(
                command.loanFacilityId(),
                command.branchCode(),
                transactionConfig,
                command.disbursementDate(),
                command.version());
    }

    private Workflow<LumpSumData> buildWorkflow() {
        return new Workflow<>() {
            @Override
            public String workflowType() {
                return "lump-sum-disbursement";
            }

            @Override
            public List<Step<LumpSumData>> steps() {
                return List.of(
                        Steps.read(
                                        LumpSumDisbursementStep.VALIDATE_FACILITY,
                                        LumpSumDisbursementCommandHandler.this::validateFacility)
                                .build(),
                        Steps.remote(
                                        LumpSumDisbursementStep.RESOLVE_ACCOUNTS,
                                        LumpSumDisbursementCommandHandler.this::resolveAccounts)
                                .retry(RetryPolicy.CONSERVATIVE)
                                .timeout(Duration.ofSeconds(30))
                                .compensatedBy(LumpSumDisbursementCommandHandler.this::closeAccounts)
                                .build(),
                        Steps.remote(
                                        LumpSumDisbursementStep.POST_TRANSACTIONS,
                                        LumpSumDisbursementCommandHandler.this::postTransactions)
                                .retry(RetryPolicy.CONSERVATIVE)
                                .timeout(Duration.ofSeconds(30))
                                .compensatedBy(LumpSumDisbursementCommandHandler.this::reverseTransactions)
                                .build(),
                        Steps.write(
                                        LumpSumDisbursementStep.APPLY_DISBURSEMENT,
                                        LumpSumDisbursementCommandHandler.this::applyDisbursement)
                                .compensatedBy(LumpSumDisbursementCommandHandler.this::revertDisbursement)
                                .build());
            }
        };
    }

    private StepResult<Void> validateFacility(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();

        var validationResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility -> loadInstallmentSchedule(facility)
                        .flatMap(schedule -> validateDisbursement(facility, schedule, data)));

        return StepResult.fromResult(validationResult);
    }

    private StepResult<Void> resolveAccounts(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();

        var result = loadFacility(LoanFacilityId.of(data.facilityId())).flatMap(facility -> loadLoanType(facility)
                .flatMap(loanType -> loadLoanArrangement(facility).flatMap(arrangement -> {
                    Set<TradeRelationType> requiredRelationTypes =
                            strategyProvider.getAllRequiredRelationTypes(facility);

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

        Map<String, String> serializedAccounts = result.unwrap().accountsByRelationType().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), e -> e.getValue().value()));

        ctx.updateData(d -> d.withResolvedAccounts(serializedAccounts));

        return new StepResult.Success<>(null);
    }

    private StepResult<Void> closeAccounts(WorkflowContext<LumpSumData> ctx) {
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

    private StepResult<Void> postTransactions(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();
        ResolvedAccounts resolvedAccounts = data.getResolvedAccounts();

        var transactionsResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanType(facility).flatMap(loanType -> loadLoanArrangement(facility)
                        .flatMap(arrangement -> loadInstallmentSchedule(facility)
                                .flatMap(schedule -> schedule.activateSchedule(clock)
                                        .flatMap(ignored -> createTransactions(
                                                facility, loanType, arrangement, schedule, data, resolvedAccounts))))));

        if (transactionsResult.isFailure()) {
            return StepResult.failure(transactionsResult.err().orElseThrow());
        }

        var postResult = transactionPostingPort.postTransactions(
                LoanFacilityId.of(data.facilityId()),
                configuration.getFcbMergedDocumentTitle(),
                transactionsResult.unwrap());

        if (postResult.isFailure()) {
            return StepResult.failure(postResult.err().orElseThrow());
        }

        List<LumpSumData.PostedTransactionData> posted = postResult.unwrap().stream()
                .map(tracked -> new LumpSumData.PostedTransactionData(
                        tracked.value(), tracked.trackingId(), tracked.status()))
                .toList();

        ctx.updateData(d -> d.withPostedTransactions(posted));

        List<String> transactionNumbers = posted.stream()
                .map(LumpSumData.PostedTransactionData::transactionNumber)
                .toList();
        log.info("Transactions posted for facility {}: {}", data.facilityId(), transactionNumbers);
        return new StepResult.Success<>(null);
    }

    private StepResult<Void> reverseTransactions(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();
        log.warn("Reversing transactions for facility {}", data.facilityId());

        List<TrackedTransactionNumber> trackedNumbers = rebuildTrackedNumbers(data);
        if (trackedNumbers.isEmpty()) {
            return new StepResult.Success<>(null);
        }

        return StepResult.fromResult(transactionPostingPort.reverseTransactions(trackedNumbers));
    }

    private StepResult<List<DomainEvent<?>>> applyDisbursement(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();

        var result = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanArrangement(facility)
                        .flatMap(arrangement -> loadInstallmentSchedule(facility)
                                .flatMap(schedule -> schedule.activateSchedule(clock)
                                        .flatMap(ignored -> disburse(facility, arrangement, data))
                                        .map(ignored -> {
                                            List<DomainEvent<?>> events = new ArrayList<>();
                                            events.addAll(schedule.domainEvents());
                                            events.addAll(facility.domainEvents());

                                            installmentScheduleRepository.save(schedule);
                                            facilityRepository.save(facility, data.expectedVersion());

                                            log.info(
                                                    "Lump sum disbursement applied: facilityId={}", data.facilityId());
                                            return events;
                                        }))));

        return StepResult.fromWriteResult(result);
    }

    private Result<Unit> disburse(TradeLoanFacility facility, TradeLoanArrangement arrangement, LumpSumData data) {

        List<TrackedTransactionNumber> trackedNumbers = rebuildTrackedNumbers(data);

        return facility.getSanctionedLoan()
                .map(AbstractSanctionedLoan::getApprovedAmount)
                .map(approvedAmount -> facility.lumpSumDisbursement(
                                approvedAmount,
                                trackedNumbers,
                                data.getResolvedAccounts().accountsByRelationType(),
                                Objects.requireNonNull(arrangement.getInstallmentPolicy())
                                        .installmentPaymentType(),
                                clock,
                                data.disbursementDate())
                        .map(ignored -> Unit.INSTANCE))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.SANCTIONED_LOAN_NOT_FOUND,
                        facility.getId().value())));
    }

    private StepResult<Void> revertDisbursement(WorkflowContext<LumpSumData> ctx) {
        var data = ctx.data();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }

        var facility = facilityResult.unwrap();
        var revertResult = facility.revertLumpSumDisbursement(clock);
        if (revertResult.isFailure()) {
            return StepResult.fromResult(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted lump sum disbursement: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private List<TrackedTransactionNumber> rebuildTrackedNumbers(LumpSumData data) {
        if (data.postedTransactions() == null) {
            return List.of();
        }
        return data.postedTransactions().stream()
                .map(posted -> TrackedTransactionNumber.create(
                        posted.transactionNumber(), posted.trackingId(), posted.status(), clock))
                .toList();
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.LUMP_SUM)
                .map(ignored -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod() != null
                                        ? sl.getDisbursementMethod().name()
                                        : "null")
                                .orElse("UNKNOWN"))));
    }

    private Result<Unit> validateDisbursement(
            TradeLoanFacility facility, InstallmentSchedule schedule, LumpSumData data) {
        if (facility.getSanctionedLoan().isEmpty()) {
            return Result.failure(Notification.ofError(
                    TradeLoanApplicationServiceErrors.SANCTIONED_LOAN_NOT_FOUND,
                    facility.getId().value()));
        }
        var sanctionedLoan = facility.getSanctionedLoan().get();
        return facility.validateLumpSumDisbursement(sanctionedLoan.getApprovedAmount())
                .flatMap(ignored -> facility.validateDisbursementDate(
                        data.disbursementDate(),
                        schedule.getInstallments().getFirst().getDueDate()));
    }

    private Result<List<LoanTransaction>> createTransactions(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            TradeLoanArrangement arrangement,
            InstallmentSchedule schedule,
            LumpSumData data,
            ResolvedAccounts resolvedAccounts) {

        return createBranchCode(data).flatMap(branchCode -> createPostTitle(facility)
                .flatMap(postTitle -> DocumentMetadataUtils.createBaseArticleMetadata(
                                facility,
                                loanType,
                                branchCode,
                                data.transactionConfig(),
                                DocumentMetadataType.DISBURSEMENT)
                        .flatMap(metadata -> transactionService.createTransactions(
                                facility,
                                arrangement,
                                loanType,
                                branchCode,
                                postTitle,
                                metadata,
                                schedule,
                                resolvedAccounts))));
    }

    private Result<BranchCode> createBranchCode(LumpSumData data) {
        return BranchCode.of(data.branchCode());
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        return PostTitle.of(
                configuration.getPostTitleTemplate().formatted(facility.getId().value()));
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanTypeRepository.findById(facility.getLoanTypeId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value())));
    }

    private Result<TradeLoanArrangement> loadLoanArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanArrangementRepository.findById(facility.getLoanArrangementId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                        facility.getLoanArrangementId(),
                        facility.getId().value())));
    }

    private Result<InstallmentSchedule> loadInstallmentSchedule(TradeLoanFacility facility) {
        return facility.getInstallmentScheduleId()
                .map(scheduleId -> Result.fromOptional(
                        installmentScheduleRepository.findById(scheduleId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                                facility.getId().value()))))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        facility.getId().value())));
    }
}
