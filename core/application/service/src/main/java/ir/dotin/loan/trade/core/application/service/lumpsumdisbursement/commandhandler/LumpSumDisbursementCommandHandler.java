package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.entity.AbstractSanctionedLoan;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.metadata.ArticleMetadata;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.configuration.LumpSumDisbursementConfiguration;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.i18n.LumpSumDisbursementErrorCodes;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.transaction.TradeLampSunDisbursementTransactionService;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LumpSumDisbursementCommandHandler implements CommandHandler<LumpSumDisbursementCommand> {

    private static final Logger log = LoggerFactory.getLogger(LumpSumDisbursementCommandHandler.class);

    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final TradeLoanTypeRepository tradeLoanTypeRepository;
    private final TradeLoanArrangementRepository tradeLoanArrangementRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final TradeLampSunDisbursementTransactionService transactionService;
    private final TransactionPostingPort transactionPostingPort;
    private final LumpSumDisbursementConfiguration configuration;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(LumpSumDisbursementCommand command) {
        log.info("Starting lump sum disbursement for facility: {}", command.loanFacilityId());

        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return loadFacility(loanFacilityId)
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility ->
                        loadDependencies(facility, command).flatMap(context -> processDisbursement(facility, context)))
                .flatMap(this::persistAndCollectEvents)
                .peekValue(result ->
                        log.info("Lump sum disbursement completed for facility: {}", command.loanFacilityId()))
                .map(DisbursementResult::events);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findById(loanFacilityId),
                () -> Notification.ofError(LumpSumDisbursementErrorCodes.FACILITY_NOT_FOUND, loanFacilityId.value()));
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.LUMP_SUM)
                .map(ignored -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        LumpSumDisbursementErrorCodes.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(AbstractSanctionedLoan::getDisbursementMethod)
                                .orElse(null))));
    }

    private Result<ProcessingContext> loadDependencies(TradeLoanFacility facility, LumpSumDisbursementCommand command) {

        return loadLoanType(facility).flatMap(loanType -> loadLoanArrangement(facility)
                .flatMap(arrangement -> loadInstallmentSchedule(facility).flatMap(schedule -> createBranchCode(command)
                        .flatMap(branchCode -> createTransactionConfig(command)
                                .flatMap(config -> createPostTitle(facility)
                                        .map(postTitle -> new ProcessingContext(
                                                loanType, arrangement, schedule, branchCode, config, postTitle)))))));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                tradeLoanTypeRepository.findById(facility.getLoanTypeId()),
                () -> Notification.ofError(
                        LumpSumDisbursementErrorCodes.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value()));
    }

    private Result<TradeLoanArrangement> loadLoanArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                tradeLoanArrangementRepository.findById(facility.getLoanArrangementId()),
                () -> Notification.ofError(
                        LumpSumDisbursementErrorCodes.LOAN_ARRANGEMENT_NOT_FOUND,
                        facility.getLoanArrangementId(),
                        facility.getId().value()));
    }

    private Result<InstallmentSchedule> loadInstallmentSchedule(TradeLoanFacility facility) {
        return facility.getInstallmentScheduleId()
                .map(scheduleId -> Result.fromOptional(
                        installmentScheduleRepository.findById(scheduleId),
                        () -> Notification.ofError(
                                LumpSumDisbursementErrorCodes.INSTALLMENT_SCHEDULE_NOT_FOUND,
                                facility.getId().value())))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        LumpSumDisbursementErrorCodes.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        facility.getId().value())));
    }

    private Result<BranchCode> createBranchCode(LumpSumDisbursementCommand command) {
        return BranchCode.of(command.branchCode())
                .flatMapOptional(
                        Optional::ofNullable,
                        Notification.ofError(LumpSumDisbursementErrorCodes.INVALID_AMOUNT, command.branchCode()));
    }

    private Result<TransactionConfig> createTransactionConfig(LumpSumDisbursementCommand command) {
        return Result.success(new TransactionConfig(
                command.terminalType(),
                command.terminalIp(),
                command.productCode(),
                command.userId(),
                command.toolSource(),
                command.networkType(),
                command.channel()));
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        return PostTitle.of(
                configuration.postTitleTemplate().formatted(facility.getId().value()));
    }

    private Result<DisbursementOperationResult> processDisbursement(
            TradeLoanFacility facility, ProcessingContext context) {

        return createBaseMetadata(facility, context)
                .flatMap(metadata -> createTransactions(facility, context, metadata))
                .flatMap(this::postTransactionsInParallel)
                .flatMap(transactionResults -> performDisbursementOperations(facility, context, transactionResults));
    }

    private Result<ArticleMetadata> createBaseMetadata(TradeLoanFacility facility, ProcessingContext context) {
        return DocumentMetadataUtils.createBaseArticleMetadata(
                facility, context.loanType(), context.branchCode(), context.config());
    }

    private Result<List<LoanTransaction>> createTransactions(
            TradeLoanFacility facility, ProcessingContext context, ArticleMetadata metadata) {

        return transactionService.createTransactions(
                facility,
                context.arrangement(),
                context.loanType(),
                context.branchCode(),
                context.postTitle(),
                metadata,
                context.schedule());
    }

    private Result<List<TransactionResult>> postTransactionsInParallel(List<LoanTransaction> transactions) {
        List<Result<TransactionResult>> futures =
                transactions.stream().map(this::postTransaction).toList();

        return collectTransactionResults(futures);
    }

    private Result<TransactionResult> postTransaction(LoanTransaction transaction) {
        return transactionPostingPort
                .postTransaction(transaction)
                .map(trackedNumber ->
                        new TransactionResult(trackedNumber, transaction.extractAccountIdsByRelationType()));
    }

    private Result<List<TransactionResult>> collectTransactionResults(List<Result<TransactionResult>> resultList) {

        List<TransactionResult> results = new ArrayList<>();
        Notification aggregatedNotification = Notification.create();

        for (Result<TransactionResult> result : resultList) {
            aggregatedNotification.merge(result.notification());
            if (result.hasValue()) {
                results.add(result.value());
            }
        }

        return aggregatedNotification.hasErrors() ? Result.failure(aggregatedNotification) : Result.success(results);
    }

    private Result<DisbursementOperationResult> performDisbursementOperations(
            TradeLoanFacility facility, ProcessingContext context, List<TransactionResult> transactionResults) {

        List<TrackedTransactionNumber> trackedNumbers = transactionResults.stream()
                .map(TransactionResult::trackedTransactionNumber)
                .toList();

        Map<RelationType<?>, AccountId> accountIds = transactionResults.stream()
                .flatMap(result -> result.accountIds().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));

        return facility.getSanctionedLoan()
                .map(AbstractSanctionedLoan::getApprovedAmount)
                .map(approvedAmount -> performScheduleOperationBeforeDisbursement(context.schedule())
                        .flatMap(ignored -> facility.disbursement(approvedAmount, trackedNumbers, accountIds, clock))
                        .flatMap(ignored -> performScheduleOperationAfterDisbursement(context.schedule()))
                        .map(ignored -> new DisbursementOperationResult(facility, context.schedule())))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        LumpSumDisbursementErrorCodes.SANCTIONED_LOAN_NOT_FOUND,
                        facility.getId().value())));
    }

    private Result<Void> performScheduleOperationBeforeDisbursement(InstallmentSchedule schedule) {
        return Result.success();
    }

    private Result<Void> performScheduleOperationAfterDisbursement(InstallmentSchedule schedule) {
        return schedule.activateSchedule(clock).map(ignored -> null);
    }

    private Result<DisbursementResult> persistAndCollectEvents(DisbursementOperationResult operationResult) {
        installmentScheduleRepository.save(operationResult.schedule());
        log.debug(
                "Installment schedule saved: {}",
                operationResult.schedule().getId().value());

        tradeLoanFacilityRepository.save(operationResult.facility());
        log.info("Facility saved: {}", operationResult.facility().getId().value());

        List<DomainEvent<?, ?>> allEvents = aggregateEvents(operationResult);

        return Result.success(
                new DisbursementResult(operationResult.facility(), operationResult.schedule(), allEvents));
    }

    private List<DomainEvent<?, ?>> aggregateEvents(DisbursementOperationResult result) {
        List<DomainEvent<?, ?>> allEvents = new ArrayList<>();
        allEvents.addAll(result.schedule().domainEvents());
        allEvents.addAll(result.facility().domainEvents());
        return allEvents;
    }

    private record ProcessingContext(
            TradeLoanType loanType,
            TradeLoanArrangement arrangement,
            InstallmentSchedule schedule,
            BranchCode branchCode,
            TransactionConfig config,
            PostTitle postTitle) {}

    private record TransactionResult(
            TrackedTransactionNumber trackedTransactionNumber, Map<RelationType<?>, AccountId> accountIds) {}

    private record DisbursementOperationResult(TradeLoanFacility facility, InstallmentSchedule schedule) {}

    private record DisbursementResult(
            TradeLoanFacility facility, InstallmentSchedule schedule, List<DomainEvent<?, ?>> events) {}
}
