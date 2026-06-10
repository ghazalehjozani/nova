package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.saga;

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
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.saga.api.annotation.SagaHandler;
import ir.dotin.platform.pangaea.saga.api.context.SagaContext;
import ir.dotin.platform.pangaea.saga.api.definition.SagaDefinition;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.platform.pangaea.saga.api.definition.SagaStep;
import ir.dotin.platform.pangaea.saga.api.definition.SagaSteps;
import ir.dotin.platform.pangaea.saga.api.model.ResultStepAdapter;
import ir.dotin.platform.pangaea.saga.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.configuration.LumpSumDisbursementConfiguration;
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DocumentMetadataType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.TradeLumpSumDisbursementTransactionService;

import lombok.RequiredArgsConstructor;

@SagaHandler
@Component
@RequiredArgsConstructor
public class LumpSumDisbursementSaga implements SagaDefinition<LumpSumDisbursementSagaData> {

    private static final Logger log = LoggerFactory.getLogger(LumpSumDisbursementSaga.class);

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
    private final Clock clock;

    @Override
    public String sagaType() {
        return "lump-sum-disbursement";
    }

    @Override
    public List<SagaStep<LumpSumDisbursementSagaData, ?>> steps() {
        return List.of(
                SagaSteps.readOnlyStep(LumpSumDisbursementStep.VALIDATE_FACILITY, this::validateFacility)
                        .withNoRetry(),
                SagaSteps.step(LumpSumDisbursementStep.RESOLVE_ACCOUNTS, this::resolveAccounts, this::closeAccounts)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(30)),
                SagaSteps.step(
                                LumpSumDisbursementStep.POST_TRANSACTIONS,
                                this::postTransactions,
                                this::reverseTransactions)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(30)),
                SagaSteps.writeStep(
                        LumpSumDisbursementStep.APPLY_DISBURSEMENT, this::applyDisbursement, this::revertDisbursement));
    }

    @Override
    public LumpSumDisbursementSagaData createInitialData(SagaInput input) {
        var disbursementInput = (LumpSumDisbursementInput) input;
        return LumpSumDisbursementSagaData.initial(
                disbursementInput.facilityId(),
                disbursementInput.branchCode(),
                disbursementInput.transactionConfig(),
                disbursementInput.disbursementDate(),
                disbursementInput.expectedVersion());
    }

    private StepResult<Void> validateFacility(SagaContext<LumpSumDisbursementSagaData> ctx) {
        var data = ctx.getSagaData();

        var validationResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility -> loadInstallmentSchedule(facility)
                        .flatMap(schedule -> validateDisbursement(facility, schedule, data)));

        return ResultStepAdapter.toStepResultVoid(validationResult);
    }

    private StepResult<Map<String, String>> resolveAccounts(SagaContext<LumpSumDisbursementSagaData> ctx) {
        var data = ctx.getSagaData();

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
            return new StepResult.Failure<>(result.err().orElseThrow());
        }

        Map<String, String> serializedAccounts = result.unwrap().accountsByRelationType().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), e -> e.getValue().value()));

        ctx.updateSagaData(d -> d.withResolvedAccounts(serializedAccounts));

        return new StepResult.Success<>(serializedAccounts);
    }

    private StepResult<Void> closeAccounts(
            SagaContext<LumpSumDisbursementSagaData> ctx, Map<String, String> openedAccounts) {
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

    private StepResult<List<String>> postTransactions(SagaContext<LumpSumDisbursementSagaData> ctx) {
        var data = ctx.getSagaData();
        ResolvedAccounts resolvedAccounts = data.getResolvedAccounts();

        var transactionsResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanType(facility).flatMap(loanType -> loadLoanArrangement(facility)
                        .flatMap(arrangement -> loadInstallmentSchedule(facility)
                                .flatMap(schedule -> schedule.activateSchedule(clock)
                                        .flatMap(ignored -> createTransactions(
                                                facility, loanType, arrangement, schedule, data, resolvedAccounts))))));

        if (transactionsResult.isFailure()) {
            return new StepResult.Failure<>(transactionsResult.err().orElseThrow());
        }

        var postResult = transactionPostingPort.postTransactions(
                LoanFacilityId.of(data.facilityId()),
                configuration.getFcbMergedDocumentTitle(),
                transactionsResult.unwrap());

        if (postResult.isFailure()) {
            return new StepResult.Failure<>(postResult.err().orElseThrow());
        }

        List<LumpSumDisbursementSagaData.PostedTransactionData> posted = postResult.unwrap().stream()
                .map(tracked -> new LumpSumDisbursementSagaData.PostedTransactionData(
                        tracked.value(), tracked.trackingId(), tracked.status()))
                .toList();

        ctx.updateSagaData(d -> d.withPostedTransactions(posted));

        List<String> transactionNumbers = posted.stream()
                .map(LumpSumDisbursementSagaData.PostedTransactionData::transactionNumber)
                .toList();
        log.info("Transactions posted for facility {}: {}", data.facilityId(), transactionNumbers);
        return new StepResult.Success<>(transactionNumbers);
    }

    private StepResult<Void> reverseTransactions(
            SagaContext<LumpSumDisbursementSagaData> ctx, List<String> postedNumbers) {
        var data = ctx.getSagaData();
        log.warn("Reversing transactions for facility {}: {}", data.facilityId(), postedNumbers);

        List<TrackedTransactionNumber> trackedNumbers = rebuildTrackedNumbers(data);
        if (trackedNumbers.isEmpty()) {
            return new StepResult.Success<>(null);
        }

        return ResultStepAdapter.toStepResultVoid(transactionPostingPort.reverseTransactions(trackedNumbers));
    }

    private StepResult<Void> applyDisbursement(SagaContext<LumpSumDisbursementSagaData> ctx) {
        var data = ctx.getSagaData();

        var applyResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanArrangement(facility)
                        .flatMap(arrangement -> loadInstallmentSchedule(facility)
                                .flatMap(schedule -> schedule.activateSchedule(clock)
                                        .flatMap(ignored -> disburse(facility, arrangement, data))
                                        .map(ignored -> new AppliedDisbursement(facility, schedule)))));

        if (applyResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(applyResult);
        }

        AppliedDisbursement applied = applyResult.unwrap();

        List<DomainEvent<?>> events = new ArrayList<>();
        events.addAll(applied.schedule().domainEvents());
        events.addAll(applied.facility().domainEvents());

        installmentScheduleRepository.save(applied.schedule());
        facilityRepository.save(applied.facility(), data.expectedVersion());

        log.info("Lump sum disbursement applied: facilityId={}", data.facilityId());
        return StepResult.Success.of(null, events);
    }

    private Result<Unit> disburse(
            TradeLoanFacility facility, TradeLoanArrangement arrangement, LumpSumDisbursementSagaData data) {

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

    private StepResult<Void> revertDisbursement(SagaContext<LumpSumDisbursementSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.unwrap();
        var revertResult = facility.revertLumpSumDisbursement(clock);
        if (revertResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted lump sum disbursement: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private List<TrackedTransactionNumber> rebuildTrackedNumbers(LumpSumDisbursementSagaData data) {
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
            TradeLoanFacility facility, InstallmentSchedule schedule, LumpSumDisbursementSagaData data) {
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
            LumpSumDisbursementSagaData data,
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

    private Result<BranchCode> createBranchCode(LumpSumDisbursementSagaData data) {
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

    private record AppliedDisbursement(TradeLoanFacility facility, InstallmentSchedule schedule) {}
}
