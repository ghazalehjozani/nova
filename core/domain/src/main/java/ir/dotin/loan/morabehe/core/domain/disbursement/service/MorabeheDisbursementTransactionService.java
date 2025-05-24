package ir.dotin.loan.morabehe.core.domain.disbursement.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.aggregate.AbstractLoanApplication;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.ExternalTransactionPostingClient;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.LoanTopicResolver;
import ir.dotin.loan.baseloan.core.domain.shared.service.transaction.TransactionFactory;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.DocumentCalculationStrategy;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.PostTitle;
import ir.dotin.loan.baseloan.core.domain.shared.vo.transaction.TransactionNumber;
import ir.dotin.loan.morabehe.core.domain.disbursement.strategy.*;
import ir.dotin.loan.morabehe.core.domain.loanfacility.aggregate.MorabeheLoanFacility;
import ir.dotin.loan.morabehe.core.domain.loanfacility.i18n.MorabeheLoanFacilityLocalizedMessageCodes;

@DomainService
public final class MorabeheDisbursementTransactionService {

    private final TransactionFactory transactionFactory;
    private final ExternalTransactionPostingClient externalTransactionPostingClient;
    private final LoanTopicResolver loanTopicResolver;
    private final DisbursementStrategyProvider strategyProvider;

    public MorabeheDisbursementTransactionService(
            TransactionFactory transactionFactory,
            ExternalTransactionPostingClient externalTransactionPostingClient,
            LoanTopicResolver loanTopicResolver,
            DisbursementStrategyProvider strategyProvider) {
        this.transactionFactory = Objects.requireNonNull(transactionFactory);
        this.externalTransactionPostingClient = Objects.requireNonNull(externalTransactionPostingClient);
        this.loanTopicResolver = Objects.requireNonNull(loanTopicResolver);
        this.strategyProvider = Objects.requireNonNull(strategyProvider);
    }

    public Result<List<TransactionNumber>> generateAndPostDisbursementTransactions(MorabeheLoanFacility facility) {
        Objects.requireNonNull(facility, "facility cannot be null");

        Result<Void> validationResult = validateFacilityForDisbursement(facility);
        if (validationResult.isFailure()) {
            return Result.failure(validationResult.notification());
        }

        try {
            Result<CalculationContext<MorabeheLoanFacility>> contextResult = prepareCalculationContext(facility);
            if (contextResult.isFailure()) {
                return Result.failure(contextResult.notification());
            }

            List<DocumentCalculationStrategy<MorabeheLoanFacility>> applicableStrategies =
                    strategyProvider.getStrategies(facility);
            if (applicableStrategies.isEmpty()) {
                return Result.success(Collections.emptyList());
            }

            List<CompletableFuture<Result<TransactionData>>> transactionFutures = applicableStrategies.stream()
                    .map(strategy ->
                            CompletableFuture.supplyAsync(() -> createTransactionData(contextResult.value(), strategy)))
                    .toList();

            CompletableFuture<List<Result<TransactionData>>> allTransactionsFuture = CompletableFuture.allOf(
                            transactionFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> transactionFutures.stream()
                            .map(CompletableFuture::join)
                            .toList());

            List<Result<TransactionData>> transactionResults = allTransactionsFuture.join();

            Notification accumulatedNotification = Notification.create();
            for (Result<TransactionData> result : transactionResults) {
                if (result.isFailure()) {
                    accumulatedNotification.merge(result.notification());
                }
            }

            if (!accumulatedNotification.isEmpty()) {
                return Result.failure(accumulatedNotification);
            }

            List<TransactionData> validTransactions = transactionResults.stream()
                    .filter(Result::isSuccess)
                    .map(Result::value)
                    .filter(data -> data != null
                            && data.transaction != null
                            && data.transaction.document() != null
                            && !data.transaction.document().articles().isEmpty())
                    .toList();

            if (validTransactions.isEmpty()) {
                return Result.success(Collections.emptyList());
            }

            List<CompletableFuture<Result<TransactionNumber>>> postingFutures = validTransactions.stream()
                    .map(data -> CompletableFuture.supplyAsync(() -> postTransaction(data)))
                    .toList();

            CompletableFuture<List<Result<TransactionNumber>>> allPostingsFuture = CompletableFuture.allOf(
                            postingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v ->
                            postingFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

            List<Result<TransactionNumber>> postingResults = allPostingsFuture.join();

            accumulatedNotification = Notification.create();
            for (int i = 0; i < postingResults.size(); i++) {
                Result<TransactionNumber> result = postingResults.get(i);
                if (result.isFailure()) {
                    TransactionData data = validTransactions.get(i);
                    accumulatedNotification.merge(result.notification());
                    accumulatedNotification.addError(
                            MorabeheLoanFacilityLocalizedMessageCodes.TRANSACTION_POSTING_FAILED, data.purposeCode);
                }
            }

            if (!accumulatedNotification.isEmpty()) {
                return Result.failure(accumulatedNotification);
            }

            List<TransactionNumber> transactionNumbers = postingResults.stream()
                    .filter(Result::isSuccess)
                    .map(Result::value)
                    .toList();

            return Result.success(transactionNumbers);
        } catch (Exception e) {
            Notification notification = Notification.create()
                    .addError(MorabeheLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, e.getMessage());
            return Result.failure(notification);
        }
    }

    private Result<Void> validateFacilityForDisbursement(MorabeheLoanFacility facility) {
        Notification notification = Notification.create();
        FacilityStatus currentState = facility.getCurrentState();

        if (currentState != FacilityStatus.PENDING_DISBURSEMENT) {
            notification = notification.addError(
                    MorabeheLoanFacilityLocalizedMessageCodes.CANNOT_POST_DISBURSE_TRANSACTION, currentState.name());
            return Result.failure(notification);
        }

        return Result.success();
    }

    private Result<CalculationContext<MorabeheLoanFacility>> prepareCalculationContext(MorabeheLoanFacility facility) {
        Objects.requireNonNull(facility, "MorabeheLoanFacility cannot be null for preparing CalculationContext.");
        Notification accumulatedNotification = Notification.create();

        // 1. Resolve LoanTopic
        AbstractLoanApplication<?, ?> loanApplication = facility.getLoanApplication();
        if (loanApplication == null) {
            accumulatedNotification = accumulatedNotification.addError(
                    MorabeheLoanFacilityLocalizedMessageCodes.CALCULATION_CONTEXT_LOAN_APPLICATION_MISSING_IN_FACILITY);
            return Result.failure(accumulatedNotification);
        }

        EconomicSector economicSector = loanApplication.getEconomicSector();
        if (economicSector == null) {
            accumulatedNotification = accumulatedNotification.addError(
                    MorabeheLoanFacilityLocalizedMessageCodes.CALCULATION_CONTEXT_ECONOMIC_SECTOR_MISSING);
            return Result.failure(accumulatedNotification);
        }

        LoanTopicResolver.Input resolverInput = new LoanTopicResolver.Input(facility.loanTypeId(), economicSector);
        Result<LoanTopic> topicResult = loanTopicResolver.resolvePrimaryTopic(resolverInput);

        if (topicResult.isFailure()) {
            accumulatedNotification.merge(topicResult.notification());
            accumulatedNotification = accumulatedNotification.addError(
                    MorabeheLoanFacilityLocalizedMessageCodes.LOAN_TOPIC_RESOLUTION_FAILED,
                    facility.loanTypeId().toString(),
                    economicSector.toString());
            return Result.failure(accumulatedNotification);
        }
        LoanTopic primaryLoanTopic = topicResult.value();

        // 2. Prepare PostTitle
        Optional<ApplicationNumber> appNumberOpt = loanApplication.getApplicationNumber();
        if (appNumberOpt.isEmpty()) {
            accumulatedNotification = accumulatedNotification.addError(
                    MorabeheLoanFacilityLocalizedMessageCodes.CALCULATION_CONTEXT_APPLICATION_NUMBER_MISSING);
            return Result.failure(accumulatedNotification);
        }
        ApplicationNumber applicationNumber = appNumberOpt.orElseThrow();

        Result<PostTitle> postTitleResult = PostTitle.of(applicationNumber.derivedValue());
        if (postTitleResult.isFailure()) {
            accumulatedNotification.merge(postTitleResult.notification());
            accumulatedNotification = accumulatedNotification.addError(
                    MorabeheLoanFacilityLocalizedMessageCodes.CALCULATION_CONTEXT_POST_TITLE_CREATION_FAILED,
                    applicationNumber.derivedValue());
            return Result.failure(accumulatedNotification);
        }
        PostTitle postTitle = postTitleResult.value();

        Result<CalculationContext<MorabeheLoanFacility>> contextResult =
                CalculationContext.of(facility, primaryLoanTopic, postTitle);

        if (contextResult.isFailure()) {
            accumulatedNotification = accumulatedNotification.merge(contextResult.notification());
            return Result.failure(accumulatedNotification);
        }

        return contextResult;
    }

    private record TransactionData(LoanTransaction transaction, String purposeCode) {}

    private Result<TransactionData> createTransactionData(
            CalculationContext<MorabeheLoanFacility> context,
            DocumentCalculationStrategy<MorabeheLoanFacility> strategy) {

        Result<LoanTransaction> transactionResult =
                transactionFactory.createTransaction(context, strategy, "postTitle");

        if (transactionResult.isFailure()) {
            return Result.failure(transactionResult.notification());
        }

        LoanTransaction transaction = transactionResult.value();

        String purposeCode = determinePurposeCode(strategy);
        if (purposeCode == null) {
            Notification notification = Notification.create()
                    .addError(
                            MorabeheLoanFacilityLocalizedMessageCodes.UNKNOWN_STRATEGY_TYPE,
                            strategy.getClass().getName());
            return Result.failure(notification);
        }

        return Result.success(new TransactionData(transaction, purposeCode));
    }

    private Result<TransactionNumber> postTransaction(TransactionData data) {
        return externalTransactionPostingClient.postTransaction(data.transaction);
    }

    private String determinePurposeCode(DocumentCalculationStrategy<MorabeheLoanFacility> strategy) {
        if (strategy instanceof CommitmentHandlingStrategy) {
            return "COMMITMENT";
        } else if (strategy instanceof CashMovementStrategy) {
            return "CASH_DISBURSE";
        } else if (strategy instanceof InterestSetupStrategy) {
            return "INTEREST_SETUP";
        } else if (strategy instanceof InsuranceCalculationStrategy) {
            return "INSURANCE";
        } else if (strategy instanceof AddedValueCalculationStrategy) {
            return "ADDED_VALUE";
        }
        return null;
    }
}
