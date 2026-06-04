package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.commandhandler;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.accounting.document.api.model.metadata.ArticleMetadata;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
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
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.DisbursementStrategyProvider;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.TradeLampSunDisbursementTransactionService;

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
    private final DisbursementStrategyProvider strategyProvider;
    private final LoanTopicResolver loanTopicResolver;
    private final AccountResolutionService accountResolutionService;
    private final BranchAccessValidator branchAccessValidator;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(LumpSumDisbursementCommand command) {
        log.info("Starting lump sum disbursement for facility: {}", command.loanFacilityId());

        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return loadFacility(loanFacilityId)
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility -> loadDependencies(facility, command)
                        .flatMap(context -> validateDisbursement(facility, context)
                                .flatMap(ignored -> resolveAccounts(facility, context))
                                .flatMap(resolvedAccounts -> processDisbursement(facility, context, resolvedAccounts))))
                .flatMap(operationResult -> persistAndCollectEvents(operationResult, command.version()))
                .onSuccess(result ->
                        log.info("Lump sum disbursement completed for facility: {}", command.loanFacilityId()))
                .map(DisbursementResult::events);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
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

    private Result<ProcessingContext> loadDependencies(TradeLoanFacility facility, LumpSumDisbursementCommand command) {
        return loadLoanType(facility).flatMap(loanType -> loadLoanArrangement(facility)
                .flatMap(arrangement -> loadInstallmentSchedule(facility).flatMap(schedule -> createBranchCode(command)
                        .flatMap(branchCode -> createTransactionConfig(command)
                                .flatMap(config -> createPostTitle(facility)
                                        .map(postTitle -> new ProcessingContext(
                                                loanType,
                                                arrangement,
                                                schedule,
                                                branchCode,
                                                config,
                                                command.disbursementDate(),
                                                postTitle)))))));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                tradeLoanTypeRepository.findById(facility.getLoanTypeId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value())));
    }

    private Result<TradeLoanArrangement> loadLoanArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                tradeLoanArrangementRepository.findById(facility.getLoanArrangementId()),
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

    private Result<BranchCode> createBranchCode(LumpSumDisbursementCommand command) {
        return BranchCode.of(command.branchCode())
                .flatMapOptional(
                        Optional::ofNullable,
                        FailureCause.businessRule(Notification.ofError(
                                TradeLoanApplicationServiceErrors.INVALID_AMOUNT, command.branchCode())));
    }

    private Result<TransactionConfig> createTransactionConfig(LumpSumDisbursementCommand command) {
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
        return PostTitle.of(
                configuration.getPostTitleTemplate().formatted(facility.getId().value()));
    }

    private Result<Unit> validateDisbursement(TradeLoanFacility facility, ProcessingContext context) {
        if (facility.getSanctionedLoan().isEmpty()) {
            return Result.failure(Notification.ofError(
                    TradeLoanApplicationServiceErrors.SANCTIONED_LOAN_NOT_FOUND,
                    facility.getId().value()));
        }
        AbstractSanctionedLoan<TradeSanctionedLoan.Builder> sanctionedLoan =
                facility.getSanctionedLoan().get();
        return facility.validateLumpSumDisbursement(sanctionedLoan.getApprovedAmount())
                .flatMap(ignored -> facility.validateDisbursementDate(
                        context.disbursementDate(),
                        context.schedule().getInstallments().getFirst().getDueDate()));
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
            TradeLoanFacility facility, ProcessingContext context, ResolvedAccounts resolvedAccounts) {

        return activateSchedule(context.schedule())
                .flatMap(ignored -> createBaseMetadata(facility, context))
                .flatMap(metadata -> createTransactions(facility, context, metadata, resolvedAccounts))
                .flatMap(loanTransactions -> postTransactionsInBatch(facility.getId(), loanTransactions))
                .flatMap(trackedNumbers ->
                        performDisbursementOperations(facility, context, trackedNumbers, resolvedAccounts));
    }

    private Result<Unit> activateSchedule(InstallmentSchedule schedule) {
        return schedule.activateSchedule(clock).map(ignored -> Unit.INSTANCE);
    }

    private Result<ArticleMetadata> createBaseMetadata(TradeLoanFacility facility, ProcessingContext context) {
        return DocumentMetadataUtils.createBaseArticleMetadata(
                facility, context.loanType(), context.branchCode(), context.config());
    }

    private Result<List<LoanTransaction>> createTransactions(
            TradeLoanFacility facility,
            ProcessingContext context,
            ArticleMetadata metadata,
            ResolvedAccounts resolvedAccounts) {

        return transactionService.createTransactions(
                facility,
                context.arrangement(),
                context.loanType(),
                context.branchCode(),
                context.postTitle(),
                metadata,
                context.schedule(),
                resolvedAccounts);
    }

    private Result<List<TrackedTransactionNumber>> postTransactionsInBatch(
            LoanFacilityId facilityId, List<LoanTransaction> transactions) {
        return transactionPostingPort.postTransactions(
                facilityId, configuration.getFcbMergedDocumentTitle(), transactions);
    }

    private Result<DisbursementOperationResult> performDisbursementOperations(
            TradeLoanFacility facility,
            ProcessingContext context,
            List<TrackedTransactionNumber> trackedNumbers,
            ResolvedAccounts resolvedAccounts) {

        return facility.getSanctionedLoan()
                .map(AbstractSanctionedLoan::getApprovedAmount)
                .map(approvedAmount -> facility.lumpSumDisbursement(
                                approvedAmount,
                                trackedNumbers,
                                resolvedAccounts.accountsByRelationType(),
                                context.arrangement.getInstallmentPolicy().installmentPaymentType(),
                                clock,
                                context.disbursementDate())
                        .map(ignored -> new DisbursementOperationResult(facility, context.schedule())))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.SANCTIONED_LOAN_NOT_FOUND,
                        facility.getId().value())));
    }

    private Result<DisbursementResult> persistAndCollectEvents(
            DisbursementOperationResult operationResult, long expectedVersion) {
        installmentScheduleRepository.save(operationResult.schedule());
        log.debug(
                "Installment schedule saved: {}",
                operationResult.schedule().getId().value());

        tradeLoanFacilityRepository.save(operationResult.facility(), expectedVersion);
        log.info("Facility saved: {}", operationResult.facility().getId().value());

        List<DomainEvent<?>> allEvents = aggregateEvents(operationResult);

        return Result.success(
                new DisbursementResult(operationResult.facility(), operationResult.schedule(), allEvents));
    }

    private List<DomainEvent<?>> aggregateEvents(DisbursementOperationResult result) {
        List<DomainEvent<?>> allEvents = new ArrayList<>();
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
            LocalDate disbursementDate,
            PostTitle postTitle) {}

    private record DisbursementOperationResult(TradeLoanFacility facility, InstallmentSchedule schedule) {}

    private record DisbursementResult(
            TradeLoanFacility facility, InstallmentSchedule schedule, List<DomainEvent<?>> events) {}
}
