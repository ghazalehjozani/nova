package ir.dotin.loan.morabehe.core.domain.disbursement.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import ir.dotin.loan.morabehe.core.domain.disbursement.strategy.*;
import ir.dotin.platform.domain.common.Notification;
import ir.dotin.platform.domain.common.Result;
import ir.dotin.platform.domain.common.annotation.DomainService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
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
            return Result.ofNotification(validationResult.notification());
        }

        try {
            Result<CalculationContext<MorabeheLoanFacility>> contextResult = prepareCalculationContext(facility);
            if (contextResult.isFailure()) {
                return Result.ofNotification(contextResult.notification());
            }

            List<DocumentCalculationStrategy<MorabeheLoanFacility>> applicableStrategies =
                    strategyProvider.getStrategies(facility);
            if (applicableStrategies.isEmpty()) {
                return Result.ofValue(Collections.emptyList());
            }

            List<CompletableFuture<Result<TransactionData>>> transactionFutures = applicableStrategies.stream()
                    .map(strategy ->
                            CompletableFuture.supplyAsync(() -> createTransactionData(contextResult.value(), strategy)))
                    .toList();

            CompletableFuture<List<Result<TransactionData>>> allTransactionsFuture = CompletableFuture.allOf(
                            transactionFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> transactionFutures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList()));

            List<Result<TransactionData>> transactionResults = allTransactionsFuture.join();

            Notification accumulatedNotification = Notification.empty();
            for (Result<TransactionData> result : transactionResults) {
                if (result.isFailure()) {
                    accumulatedNotification = accumulatedNotification.merge(result.notification());
                }
            }

            if (!accumulatedNotification.isEmpty()) {
                return Result.ofNotification(accumulatedNotification);
            }

            List<TransactionData> validTransactions = transactionResults.stream()
                    .filter(Result::isSuccess)
                    .map(Result::value)
                    .filter(data -> data != null
                            && data.transaction != null
                            && !data.transaction.documents().isEmpty()
                            && !data.transaction.documents().getFirst().articles().isEmpty())
                    .toList();

            if (validTransactions.isEmpty()) {
                return Result.ofValue(Collections.emptyList());
            }

            List<CompletableFuture<Result<TransactionNumber>>> postingFutures = validTransactions.stream()
                    .map(data -> CompletableFuture.supplyAsync(() -> postTransaction(data)))
                    .toList();

            CompletableFuture<List<Result<TransactionNumber>>> allPostingsFuture = CompletableFuture.allOf(
                            postingFutures.toArray(new CompletableFuture[0]))
                    .thenApply(v ->
                            postingFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

            List<Result<TransactionNumber>> postingResults = allPostingsFuture.join();

            accumulatedNotification = Notification.empty();
            for (int i = 0; i < postingResults.size(); i++) {
                Result<TransactionNumber> result = postingResults.get(i);
                if (result.isFailure()) {
                    TransactionData data = validTransactions.get(i);
                    accumulatedNotification = accumulatedNotification.merge(result.notification());
                    accumulatedNotification.addError(
                            MorabeheLoanFacilityLocalizedMessageCodes.TRANSACTION_POSTING_FAILED, data.purposeCode);
                }
            }

            if (!accumulatedNotification.isEmpty()) {
                return Result.ofNotification(accumulatedNotification);
            }

            List<TransactionNumber> transactionNumbers = postingResults.stream()
                    .filter(Result::isSuccess)
                    .map(Result::value)
                    .collect(Collectors.toList());

            return Result.ofValue(transactionNumbers);
        } catch (Exception e) {
            Notification notification = Notification.empty()
                    .addError(MorabeheLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, e.getMessage());
            return Result.ofNotification(notification);
        }
    }

    private Result<Void> validateFacilityForDisbursement(MorabeheLoanFacility facility) {
        Notification notification = Notification.empty();
        FacilityStatus currentState = facility.getCurrentState();

        if (currentState != FacilityStatus.PENDING_DISBURSEMENT) {
            notification = notification.addError(
                    MorabeheLoanFacilityLocalizedMessageCodes.CANNOT_POST_DISBURSE_TRANSACTION, currentState.name());
            return Result.ofNotification(notification);
        }

        return Result.empty();
    }

    private Result<CalculationContext<MorabeheLoanFacility>> prepareCalculationContext(MorabeheLoanFacility facility) {
        Notification notification = Notification.empty();

        EconomicSector economicSector = facility.getLoanApplication().getEconomicSector();
        LoanTopicResolver.Input resolverInput = new LoanTopicResolver.Input(facility.loanTypeId(), economicSector);
        Result<LoanTopic> topicResult = loanTopicResolver.resolvePrimaryTopic(resolverInput);

        if (topicResult.isFailure()) {
            notification = notification.merge(topicResult.notification());
            notification.addError(
                    MorabeheLoanFacilityLocalizedMessageCodes.LOAN_TOPIC_RESOLUTION_FAILED,
                    facility.loanTypeId(),
                    economicSector);
            return Result.ofNotification(notification);
        }
        PostTitle postTitle = new PostTitle(facility.getLoanApplication()
                .getApplicationNumber()
                .orElseThrow()
                .value()); // TODO

        try {
            CalculationContext<MorabeheLoanFacility> context =
                    CalculationContext.of(facility, topicResult.value(), postTitle);

            return Result.ofValue(context);
        } catch (IllegalStateException e) {
            notification.addError(MorabeheLoanFacilityLocalizedMessageCodes.UNEXPECTED_ERROR, e.getMessage());
            return Result.ofNotification(notification);
        }
    }

    private record TransactionData(LoanTransaction transaction, String purposeCode) {}

    private Result<TransactionData> createTransactionData(
            CalculationContext<MorabeheLoanFacility> context,
            DocumentCalculationStrategy<MorabeheLoanFacility> strategy) {

        Result<LoanTransaction> transactionResult =
                transactionFactory.createTransaction(context, strategy, "postTitle");

        if (transactionResult.isFailure()) {
            return Result.ofNotification(transactionResult.notification());
        }

        LoanTransaction transaction = transactionResult.value();

        String purposeCode = determinePurposeCode(strategy);
        if (purposeCode == null) {
            Notification notification = Notification.empty()
                    .addError(
                            MorabeheLoanFacilityLocalizedMessageCodes.UNKNOWN_STRATEGY_TYPE,
                            strategy.getClass().getName());
            return Result.ofNotification(notification);
        }

        return Result.ofValue(new TransactionData(transaction, purposeCode));
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
