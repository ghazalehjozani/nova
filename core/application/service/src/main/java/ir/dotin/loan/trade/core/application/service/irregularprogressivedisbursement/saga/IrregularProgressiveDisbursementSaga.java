package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.saga;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.saga.api.annotation.SagaHandler;
import ir.dotin.platform.pangaea.saga.api.context.SagaContext;
import ir.dotin.platform.pangaea.saga.api.definition.SagaDefinition;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.platform.pangaea.saga.api.definition.SagaStep;
import ir.dotin.platform.pangaea.saga.api.definition.SagaSteps;
import ir.dotin.platform.pangaea.saga.api.model.ResultStepAdapter;
import ir.dotin.platform.pangaea.saga.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.InstallmentRecalculationService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.configuration.IrregularProgressiveDisbursementConfiguration;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.saga.IrregularProgressiveDisbursementSagaData.InstallmentSpecData;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.saga.IrregularProgressiveDisbursementSagaData.PostedTransactionData;
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
import ir.dotin.loan.trade.core.domain.shared.document.transaction.IrregularProgressiveDisbursementTransactionService;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@SagaHandler
@Component
@RequiredArgsConstructor
public class IrregularProgressiveDisbursementSaga implements SagaDefinition<IrregularProgressiveDisbursementSagaData> {

    private static final Logger log = LoggerFactory.getLogger(IrregularProgressiveDisbursementSaga.class);

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final IrregularProgressiveDisbursementTransactionService transactionService;
    private final InstallmentRecalculationService recalculationService;
    private final TransactionPostingPort transactionPostingPort;
    private final IrregularProgressiveDisbursementConfiguration configuration;
    private final DisbursementStrategyProvider strategyProvider;
    private final LoanTopicResolver loanTopicResolver;
    private final AccountResolutionService accountResolutionService;
    private final Clock clock;

    @Override
    public String sagaType() {
        return "irregular-progressive-disbursement";
    }

    @Override
    public List<SagaStep<IrregularProgressiveDisbursementSagaData, ?>> steps() {
        return List.of(
                SagaSteps.readOnlyStep(IrregularProgressiveDisbursementStep.VALIDATE_FACILITY, this::validateFacility)
                        .withNoRetry(),
                SagaSteps.step(
                                IrregularProgressiveDisbursementStep.RESOLVE_ACCOUNTS,
                                this::resolveAccounts,
                                this::closeAccounts)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(30)),
                SagaSteps.step(
                                IrregularProgressiveDisbursementStep.POST_TRANSACTIONS,
                                this::postTransactions,
                                this::reverseTransactions)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(30)),
                SagaSteps.writeStep(
                        IrregularProgressiveDisbursementStep.APPLY_DISBURSEMENT,
                        this::applyDisbursement,
                        this::revertDisbursement));
    }

    @Override
    public IrregularProgressiveDisbursementSagaData createInitialData(SagaInput input) {
        return IrregularProgressiveDisbursementSagaData.initial((IrregularProgressiveDisbursementInput) input);
    }

    private StepResult<Void> validateFacility(SagaContext<IrregularProgressiveDisbursementSagaData> ctx) {
        var data = ctx.getSagaData();

        var validationResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadInstallmentSchedule(facility).flatMap(schedule -> validateScheduleStatus(
                                schedule)
                        .flatMap(ignored -> facility.validateDisbursementDate(
                                requireNonNull(data.disbursementDate(), "disbursementDate"),
                                schedule.getInstallments().getFirst().getDueDate()))
                        .flatMap(ignored -> facility.validateIrregularTrancheDisbursement(trancheMoney(data)))));

        return ResultStepAdapter.toStepResultVoid(validationResult);
    }

    private StepResult<Map<String, String>> resolveAccounts(SagaContext<IrregularProgressiveDisbursementSagaData> ctx) {
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
            SagaContext<IrregularProgressiveDisbursementSagaData> ctx, Map<String, String> openedAccounts) {
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

    private StepResult<List<String>> postTransactions(SagaContext<IrregularProgressiveDisbursementSagaData> ctx) {
        var data = ctx.getSagaData();

        var transactionsResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanType(facility).flatMap(loanType -> loadInstallmentSchedule(facility)
                        .flatMap(schedule -> recalculateAndVerify(facility, schedule, data)
                                .flatMap(installments ->
                                        createTransactions(facility, loanType, schedule, installments, data)))));

        if (transactionsResult.isFailure()) {
            return new StepResult.Failure<>(transactionsResult.err().orElseThrow());
        }

        List<LoanTransaction> transactions = transactionsResult.unwrap();

        var postResult = transactionPostingPort.postTransactions(
                LoanFacilityId.of(data.facilityId()), configuration.getFcbMergedDocumentTitle(), transactions);

        if (postResult.isFailure()) {
            return new StepResult.Failure<>(postResult.err().orElseThrow());
        }

        List<PostedTransactionData> posted = postResult.unwrap().stream()
                .map(tracked -> new PostedTransactionData(tracked.value(), tracked.trackingId(), tracked.status()))
                .toList();

        Map<String, String> accountIds = extractAccountIds(transactions);

        ctx.updateSagaData(d -> d.withPostedTransactions(posted, accountIds));

        List<String> transactionNumbers =
                posted.stream().map(PostedTransactionData::transactionNumber).toList();
        log.info("Transactions posted for facility {}: {}", data.facilityId(), transactionNumbers);
        return new StepResult.Success<>(transactionNumbers);
    }

    private StepResult<Void> reverseTransactions(
            SagaContext<IrregularProgressiveDisbursementSagaData> ctx, List<String> postedNumbers) {
        var data = ctx.getSagaData();
        log.warn("Reversing transactions for facility {}: {}", data.facilityId(), postedNumbers);

        List<TrackedTransactionNumber> trackedNumbers = rebuildTrackedNumbers(data);
        if (trackedNumbers.isEmpty()) {
            return new StepResult.Success<>(null);
        }

        return ResultStepAdapter.toStepResultVoid(transactionPostingPort.reverseTransactions(trackedNumbers));
    }

    private StepResult<Void> applyDisbursement(SagaContext<IrregularProgressiveDisbursementSagaData> ctx) {
        var data = ctx.getSagaData();

        var applyResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadInstallmentSchedule(facility).flatMap(schedule -> recalculateAndVerify(
                                facility, schedule, data)
                        .flatMap(installments -> restructureAndActivate(facility, schedule, installments, data)
                                .flatMap(newSchedule -> disburse(facility, newSchedule, data)
                                        .map(ignored -> new AppliedDisbursement(facility, schedule, newSchedule))))));

        if (applyResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(applyResult);
        }

        AppliedDisbursement applied = applyResult.unwrap();

        installmentScheduleRepository.save(applied.oldSchedule());
        installmentScheduleRepository.save(applied.newSchedule());
        facilityRepository.save(applied.facility(), data.expectedVersion());

        List<DomainEvent<?>> events = new ArrayList<>();
        events.addAll(applied.oldSchedule().domainEvents());
        events.addAll(applied.newSchedule().domainEvents());
        events.addAll(applied.facility().domainEvents());

        log.info("Irregular disbursement applied: facilityId={}", data.facilityId());
        return StepResult.Success.of(null, events);
    }

    private StepResult<Void> revertDisbursement(
            SagaContext<IrregularProgressiveDisbursementSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(facilityResult);
        }

        var facility = facilityResult.unwrap();
        var revertResult = facility.revertIrregularTrancheDisbursement(clock);
        if (revertResult.isFailure()) {
            return ResultStepAdapter.toStepResultVoid(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted irregular disbursement: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<List<Installment>> recalculateAndVerify(
            TradeLoanFacility facility, InstallmentSchedule schedule, IrregularProgressiveDisbursementSagaData data) {

        Result<List<InstallmentSpec>> customPlanResult = toInstallmentSpecs(data.customPlanSpecs(), data);
        if (customPlanResult.isFailure()) {
            return Result.failure(customPlanResult.err().orElseThrow());
        }
        List<InstallmentSpec> customPlan = customPlanResult.unwrap();

        return recalculationService
                .recalculateForIrregularDisbursement(
                        schedule, facility, trancheMoney(data), customPlan.isEmpty() ? null : customPlan)
                .flatMap(installments -> matchesApprovedPlan(installments, data.approvedPlanSpecs())
                        ? Result.success(installments)
                        : Result.failure(Notification.ofError(
                                TradeLoanApplicationServiceErrors.DISBURSEMENT_PLAN_DRIFT,
                                facility.getId().value())));
    }

    private boolean matchesApprovedPlan(List<Installment> installments, List<InstallmentSpecData> approvedPlan) {
        if (installments.size() != approvedPlan.size()) {
            return false;
        }
        List<Installment> sorted = installments.stream()
                .sorted(Comparator.comparingInt(Installment::getSequenceNumber))
                .toList();
        List<InstallmentSpecData> approved = approvedPlan.stream()
                .sorted(Comparator.comparingInt(InstallmentSpecData::sequenceNumber))
                .toList();
        for (int i = 0; i < sorted.size(); i++) {
            Installment installment = sorted.get(i);
            InstallmentSpecData spec = approved.get(i);
            if (installment.getSequenceNumber() != spec.sequenceNumber()
                    || !installment.getDueDate().isEqual(spec.dueDate())
                    || installment
                                    .getScheduledAmount()
                                    .principalAmount()
                                    .value()
                                    .compareTo(spec.principalAmount())
                            != 0
                    || installment.getScheduledAmount().interestAmount().value().compareTo(spec.interestAmount())
                            != 0) {
                return false;
            }
        }
        return true;
    }

    private Result<InstallmentSchedule> restructureAndActivate(
            TradeLoanFacility facility,
            InstallmentSchedule schedule,
            List<Installment> recalculatedInstallments,
            IrregularProgressiveDisbursementSagaData data) {

        Money tranche = trancheMoney(data);
        String reason = String.format("Tranche %d disbursement: %s", data.trancheNumber(), tranche.value());
        Money totalTranche = facility.getTotalDisbursedAmount()
                .add(tranche)
                .unwrapOrThrow(c -> new IllegalStateException("Creating zero Money failed unexpectedly."));

        return schedule.restructureSchedule(
                        recalculatedInstallments,
                        reason,
                        totalTranche,
                        requireNonNull(
                                facility.getSanctionedLoan().orElseThrow().getApprovedAmount()),
                        clock,
                        requireNonNull(data.transactionConfig().userId(), "userId"))
                .flatMap(newSchedule -> newSchedule.activateSchedule(clock).map(ignored -> newSchedule));
    }

    private Result<Unit> disburse(
            TradeLoanFacility facility,
            InstallmentSchedule newSchedule,
            IrregularProgressiveDisbursementSagaData data) {

        return facility.disburseIrregularTranche(
                        trancheMoney(data),
                        rebuildTrackedNumbers(data),
                        data.getPostedAccountIds(),
                        newSchedule.getId(),
                        clock,
                        requireNonNull(data.transactionConfig().userId(), "userId"),
                        requireNonNull(data.disbursementDate(), "disbursementDate"))
                .map(ignored -> Unit.INSTANCE);
    }

    private Result<List<LoanTransaction>> createTransactions(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            InstallmentSchedule schedule,
            List<Installment> recalculatedInstallments,
            IrregularProgressiveDisbursementSagaData data) {

        return createBranchCode(data).flatMap(branchCode -> createPostTitle(facility, data)
                .flatMap(postTitle -> DocumentMetadataUtils.createBaseArticleMetadata(
                                facility,
                                loanType,
                                branchCode,
                                data.transactionConfig(),
                                DocumentMetadataType.DISBURSEMENT)
                        .flatMap(metadata -> transactionService.createTransactions(
                                facility,
                                loanType,
                                branchCode,
                                postTitle,
                                metadata,
                                schedule,
                                recalculatedInstallments,
                                trancheMoney(data),
                                data.getResolvedAccounts()))));
    }

    private Map<String, String> extractAccountIds(List<LoanTransaction> transactions) {
        Map<RelationType<?>, AccountId> allAccountIds = new LinkedHashMap<>();
        transactions.stream()
                .flatMap(tx -> tx.extractAccountIdsByRelationType().entrySet().stream())
                .forEach(entry -> allAccountIds.merge(entry.getKey(), entry.getValue(), (existing, newValue) -> {
                    if (!existing.equals(newValue)) {
                        throw new IllegalStateException(
                                "Conflicting account ID for relation type: %s, existing: %s, new: %s"
                                        .formatted(entry.getKey(), existing.value(), newValue.value()));
                    }
                    return existing;
                }));
        return allAccountIds.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), e -> e.getValue().value()));
    }

    private List<TrackedTransactionNumber> rebuildTrackedNumbers(IrregularProgressiveDisbursementSagaData data) {
        if (data.postedTransactions() == null) {
            return List.of();
        }
        return data.postedTransactions().stream()
                .map(posted -> TrackedTransactionNumber.create(
                        posted.transactionNumber(), posted.trackingId(), posted.status(), clock))
                .toList();
    }

    private Result<Unit> validateScheduleStatus(InstallmentSchedule schedule) {
        boolean isFirstDisbursement = schedule.getScheduleHistory().count() == 0;
        InstallmentScheduleStatus currentStatus = schedule.getStatus();

        if (isFirstDisbursement) {
            if (currentStatus != InstallmentScheduleStatus.DRAFT) {
                return Result.failure(
                        TradeLoanApplicationServiceErrors.INVALID_SCHEDULE_STATUS_FOR_FIRST_DISBURSEMENT,
                        currentStatus);
            }
        } else {
            if (currentStatus != InstallmentScheduleStatus.ACTIVE) {
                return Result.failure(
                        TradeLoanApplicationServiceErrors.INVALID_SCHEDULE_STATUS_FOR_SUBSEQUENT_DISBURSEMENT,
                        currentStatus);
            }
        }

        return Result.success();
    }

    private Money trancheMoney(IrregularProgressiveDisbursementSagaData data) {
        CurrencyType currencyType = CurrencyType.valueOf(data.currencyCode()).unwrap();
        return Money.valueOf(data.trancheAmount(), currencyType).unwrap();
    }

    private Result<List<InstallmentSpec>> toInstallmentSpecs(
            @Nullable List<InstallmentSpecData> specs, IrregularProgressiveDisbursementSagaData data) {
        if (specs == null) {
            return Result.success(List.of());
        }
        CurrencyType currencyType = CurrencyType.valueOf(data.currencyCode()).unwrap();
        List<InstallmentSpec> result = new ArrayList<>();
        for (InstallmentSpecData spec : specs) {
            Result<Money> principal = Money.valueOf(spec.principalAmount(), currencyType);
            Result<Money> interest = Money.valueOf(spec.interestAmount(), currencyType);
            if (principal.isFailure()) {
                return Result.failure(principal.err().orElseThrow());
            }
            if (interest.isFailure()) {
                return Result.failure(interest.err().orElseThrow());
            }
            Result<InstallmentSpec> specResult =
                    InstallmentSpec.of(spec.sequenceNumber(), principal.unwrap(), interest.unwrap(), spec.dueDate());
            if (specResult.isFailure()) {
                return Result.failure(specResult.err().orElseThrow());
            }
            result.add(specResult.unwrap());
        }
        return Result.success(result);
    }

    private Result<BranchCode> createBranchCode(IrregularProgressiveDisbursementSagaData data) {
        return BranchCode.of(data.branchCode());
    }

    private Result<PostTitle> createPostTitle(
            TradeLoanFacility facility, IrregularProgressiveDisbursementSagaData data) {
        String title =
                configuration.getPostTitleTemplate().formatted(facility.getId().value(), data.trancheNumber());
        return PostTitle.of(title);
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

    private record AppliedDisbursement(
            TradeLoanFacility facility, InstallmentSchedule oldSchedule, InstallmentSchedule newSchedule) {}
}
