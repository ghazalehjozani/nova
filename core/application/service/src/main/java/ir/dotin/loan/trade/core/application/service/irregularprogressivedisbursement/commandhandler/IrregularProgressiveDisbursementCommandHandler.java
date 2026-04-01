package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.commandhandler;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.InstallmentRecalculationService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.ArticleMetadata;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularProgressiveDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.configuration.IrregularProgressiveDisbursementConfiguration;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.mapper.IrregularProgressiveDisbursementInstallmentSchedulePlanMapper;
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.transaction.IrregularProgressiveDisbursementTransactionService;
import ir.dotin.loan.trade.core.domain.loanfacility.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class IrregularProgressiveDisbursementCommandHandler
        implements CommandHandler<IrregularProgressiveDisbursementCommand> {

    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final TradeLoanTypeRepository tradeLoanTypeRepository;
    private final TradeLoanArrangementRepository tradeLoanArrangementRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final IrregularProgressiveDisbursementTransactionService transactionService;
    private final InstallmentRecalculationService recalculationService;
    private final TransactionPostingPort transactionPostingPort;
    private final IrregularProgressiveDisbursementConfiguration configuration;
    private final IrregularProgressiveDisbursementInstallmentSchedulePlanMapper planMapper;
    private final DisbursementStrategyProvider strategyProvider;
    private final LoanTopicResolver loanTopicResolver;
    private final AccountResolutionService accountResolutionService;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(IrregularProgressiveDisbursementCommand command) {
        log.info("Starting irregular disbursement for facility: {}", command.loanFacilityId());

        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return loadFacility(loanFacilityId)
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility -> loadDependencies(facility, command)
                        .flatMap(context -> validateAll(facility, context)
                                .flatMap(ignored -> resolveAccounts(facility, context))
                                .flatMap(resolvedAccounts -> processDisbursement(context, resolvedAccounts))))
                .flatMap(this::persistAndCollectEvents)
                .peekValue(result ->
                        log.info("Irregular disbursement completed for facility: {}", command.loanFacilityId()))
                .map(DisbursementResult::events);
    }

    private Result<ResolvedAccounts> resolveAccounts(TradeLoanFacility facility, ProcessingContext context) {
        Set<TradeRelationType> requiredRelationTypes = strategyProvider.getAllRequiredRelationTypes(facility);

        Set<LoanTopic> requiredTopics = loanTopicResolver.resolveTopics(
                context.loanType(), facility.getLoanApplication().getEconomicSector(), requiredRelationTypes);

        return accountResolutionService.resolveAccounts(
                requiredTopics,
                facility.getAccountInfoMap(),
                context.arrangement().getCurrencyType().getCode());
    }

    private Result<DisbursementOperationResult> processDisbursement(
            ProcessingContext context, ResolvedAccounts resolvedAccounts) {

        return recalculateSchedule(context).flatMap(recalculatedInstallments -> restructureAndActivateSchedule(
                        context.facility(), context, recalculatedInstallments)
                .flatMap(newSchedule -> createBaseMetadata(context.facility(), context)
                        .flatMap(metadata -> createTransactions(
                                context.facility(), context, metadata, recalculatedInstallments, resolvedAccounts))
                        .flatMap(transactions ->
                                postTransactionsInBatch(context.facility().getId(), transactions))
                        .flatMap(transactionResults -> performDisbursementOperations(
                                context.facility(), context, transactionResults, newSchedule))));
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findById(loanFacilityId),
                () -> Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value()));
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.IRREGULAR_PROGRESSIVE)
                .map(ignored -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod().name())
                                .orElse("UNKNOWN"))));
    }

    private Result<ProcessingContext> loadDependencies(
            TradeLoanFacility facility, IrregularProgressiveDisbursementCommand command) {
        CurrencyType currencyType = facility.getSanctionedLoan().orElseThrow().getCurrency();
        Money trancheAmount = Money.valueOf(command.trancheAmount(), requireNonNull(currencyType))
                .getValue();

        List<InstallmentSpec> customPlan = null;
        if (command.installmentSchedulePlan() != null) {
            customPlan = planMapper.mapSpecs(command.installmentSchedulePlan().installments(), currencyType);
        }

        List<InstallmentSpec> finalCustomPlan = customPlan;

        return loadLoanType(facility).flatMap(loanType -> loadLoanArrangement(facility)
                .flatMap(arrangement -> loadInstallmentSchedule(facility).flatMap(schedule -> createBranchCode(command)
                        .flatMap(branchCode -> createTransactionConfig(command)
                                .flatMap(config -> createPostTitle(facility)
                                        .map(postTitle -> new ProcessingContext(
                                                loanType,
                                                arrangement,
                                                facility,
                                                schedule,
                                                branchCode,
                                                config,
                                                postTitle,
                                                trancheAmount,
                                                command.disbursementDate(),
                                                finalCustomPlan)))))));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                tradeLoanTypeRepository.findById(facility.getLoanTypeId()),
                () -> Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value()));
    }

    private Result<TradeLoanArrangement> loadLoanArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                tradeLoanArrangementRepository.findById(facility.getLoanArrangementId()),
                () -> Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                        facility.getLoanArrangementId(),
                        facility.getId().value()));
    }

    private Result<InstallmentSchedule> loadInstallmentSchedule(TradeLoanFacility facility) {
        return facility.getInstallmentScheduleId()
                .map(scheduleId -> Result.fromOptional(
                        installmentScheduleRepository.findById(scheduleId),
                        () -> Notification.ofError(
                                TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                                facility.getId().value())))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        facility.getId().value())));
    }

    private Result<BranchCode> createBranchCode(IrregularProgressiveDisbursementCommand command) {
        return BranchCode.of(command.branchCode())
                .flatMapOptional(
                        java.util.Optional::ofNullable,
                        Notification.ofError(
                                TradeLoanApplicationServiceErrors.INVALID_BRANCH_CODE, command.branchCode()));
    }

    private Result<TransactionConfig> createTransactionConfig(IrregularProgressiveDisbursementCommand command) {
        return Result.success(new TransactionConfig(
                command.terminalType(),
                command.terminalId(),
                command.terminalIp(),
                command.productCode(),
                command.userId(),
                command.toolSource(),
                command.networkType(),
                command.branchCode(),
                command.channel()));
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        int trancheNumber = facility.getSanctionedLoan().orElseThrow().getDisbursementCount() + 1;
        String title =
                configuration.postTitleTemplate().formatted(facility.getId().value(), trancheNumber);
        return PostTitle.of(title);
    }

    private Result<Void> validateAll(TradeLoanFacility facility, ProcessingContext context) {
        return validateScheduleStatus(context)
                .flatMap(ignored -> facility.validateDisbursementDate(
                        context.disbursementDate(),
                        context.schedule().getInstallments().getFirst().getDueDate()))
                .flatMap(ignored -> facility.validateIrregularTrancheDisbursement(context.trancheAmount()));
    }

    private Result<Void> validateScheduleStatus(ProcessingContext context) {
        boolean isFirstDisbursement = context.schedule().getScheduleHistory().count() == 0;
        InstallmentScheduleStatus currentStatus = context.schedule().getStatus();

        if (isFirstDisbursement) {
            if (currentStatus != InstallmentScheduleStatus.DRAFT) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_SCHEDULE_STATUS_FOR_FIRST_DISBURSEMENT,
                        currentStatus));
            }
        } else {
            if (currentStatus != InstallmentScheduleStatus.ACTIVE) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_SCHEDULE_STATUS_FOR_SUBSEQUENT_DISBURSEMENT,
                        currentStatus));
            }
        }

        return Result.success();
    }

    private Result<List<Installment>> recalculateSchedule(ProcessingContext context) {
        return recalculationService.recalculateForIrregularDisbursement(
                context.schedule(), context.facility(), context.trancheAmount(), context.customPlan());
    }

    private Result<InstallmentSchedule> restructureAndActivateSchedule(
            TradeLoanFacility facility, ProcessingContext context, List<Installment> recalculatedInstallments) {

        int trancheNumber = context.schedule().getScheduleHistory().count() + 1;
        String reason = String.format(
                "Tranche %d disbursement: %s",
                trancheNumber, context.trancheAmount().value());
        Money totalTranche = facility.getTotalDisbursedAmount()
                .add(context.trancheAmount())
                .orElseThrow(() -> new IllegalStateException("Creating zero Money failed unexpectedly."));

        return context.schedule()
                .restructureSchedule(
                        recalculatedInstallments,
                        reason,
                        totalTranche,
                        requireNonNull(
                                facility.getSanctionedLoan().orElseThrow().getApprovedAmount()),
                        clock,
                        context.config().userId())
                .flatMap(newSchedule -> newSchedule.activateSchedule(clock).map(ignored -> newSchedule));
    }

    private Result<ArticleMetadata> createBaseMetadata(TradeLoanFacility facility, ProcessingContext context) {
        return DocumentMetadataUtils.createBaseArticleMetadata(
                facility, context.loanType(), context.branchCode(), context.config());
    }

    private Result<List<LoanTransaction>> createTransactions(
            TradeLoanFacility facility,
            ProcessingContext context,
            ArticleMetadata metadata,
            List<Installment> recalculatedInstallments,
            ResolvedAccounts resolvedAccounts) {

        return transactionService.createTransactions(
                facility,
                context.loanType(),
                context.branchCode(),
                context.postTitle(),
                metadata,
                context.schedule(),
                recalculatedInstallments,
                context.trancheAmount(),
                resolvedAccounts);
    }

    private Result<List<TransactionResult>> postTransactionsInBatch(
            LoanFacilityId facilityId, List<LoanTransaction> transactions) {
        return transactionPostingPort
                .postTransactions(facilityId, configuration.fcbMergedDocumentTitle(), transactions)
                .map(trackedNumbers -> {
                    Map<RelationType<?>, AccountId> allAccountIds = new LinkedHashMap<>();
                    transactions.stream()
                            .flatMap(tx -> tx.extractAccountIdsByRelationType().entrySet().stream())
                            .forEach(entry ->
                                    allAccountIds.merge(entry.getKey(), entry.getValue(), (existing, newValue) -> {
                                        if (!existing.equals(newValue)) {
                                            throw new IllegalStateException(
                                                    "Conflicting account ID for relation type: %s, existing: %s, new: %s"
                                                            .formatted(
                                                                    entry.getKey(),
                                                                    existing.value(),
                                                                    newValue.value()));
                                        }
                                        return existing;
                                    }));

                    return trackedNumbers.stream()
                            .map(tracked -> new TransactionResult(tracked, allAccountIds))
                            .toList();
                });
    }

    private Result<DisbursementOperationResult> performDisbursementOperations(
            TradeLoanFacility facility,
            ProcessingContext context,
            List<TransactionResult> transactionResults,
            InstallmentSchedule newSchedule) {

        List<TrackedTransactionNumber> trackedNumbers = transactionResults.stream()
                .map(TransactionResult::trackedTransactionNumber)
                .toList();

        Map<RelationType<?>, AccountId> accountIds = transactionResults.stream()
                .flatMap(result -> result.accountIds().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));

        return facility.disburseIrregularTranche(
                        context.trancheAmount(),
                        trackedNumbers,
                        accountIds,
                        newSchedule.getId(),
                        clock,
                        context.config().userId(),
                        context.disbursementDate())
                .map(ignored -> new DisbursementOperationResult(facility, context.schedule(), newSchedule));
    }

    private Result<DisbursementResult> persistAndCollectEvents(DisbursementOperationResult operationResult) {
        installmentScheduleRepository.save(operationResult.oldSchedule());
        InstallmentSchedule newSchedule = installmentScheduleRepository.save(operationResult.newSchedule());
        TradeLoanFacility savedFacility = tradeLoanFacilityRepository.save(operationResult.facility());

        List<DomainEvent<?>> events = new ArrayList<>();
        events.addAll(operationResult.oldSchedule().domainEvents());
        events.addAll(operationResult.newSchedule().domainEvents());
        events.addAll(operationResult.facility().domainEvents());

        return Result.success(new DisbursementResult(savedFacility, newSchedule, events));
    }

    private record ProcessingContext(
            TradeLoanType loanType,
            TradeLoanArrangement arrangement,
            TradeLoanFacility facility,
            InstallmentSchedule schedule,
            BranchCode branchCode,
            TransactionConfig config,
            PostTitle postTitle,
            Money trancheAmount,
            LocalDate disbursementDate,
            List<InstallmentSpec> customPlan) {}

    private record TransactionResult(
            TrackedTransactionNumber trackedTransactionNumber, Map<RelationType<?>, AccountId> accountIds) {}

    private record DisbursementOperationResult(
            TradeLoanFacility facility, InstallmentSchedule oldSchedule, InstallmentSchedule newSchedule) {}

    private record DisbursementResult(
            TradeLoanFacility facility, InstallmentSchedule schedule, List<DomainEvent<?>> events) {}
}
