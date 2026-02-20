package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.TransactionStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.IssueDocumentRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CancelTransferMoneyResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.IssueGeneralDocumentResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.TransferMoneyResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.AccountMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.IssueDocumentRequestBuilder;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.LoanTransactionMerger;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!activemq")
@RequiredArgsConstructor
public class TransactionPostingAdapter implements TransactionPostingPort {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;
    private final IssueDocumentRequestBuilder issueDocumentRequestBuilder;
    private final Clock clock;

    @Override
    public Result<TrackedTransactionNumber> postTransaction(LoanTransaction loanTransaction) {
        UUID trackingId = UUID.randomUUID();
        log.info(
                "Issuing document from LoanTransaction - facilityId: {}, articles: {}",
                loanTransaction.loanFacilityId().value(),
                loanTransaction.document().articles().size());

        try {
            Result<IssueDocumentRequest> mappingResult =
                    AccountMapper.mapToIssueDocumentRequest(loanTransaction, trackingId);

            if (mappingResult.isFailure()) {
                log.error(
                        "Failed to map LoanTransaction to IssueDocumentRequest: {}",
                        mappingResult.notification().getErrorMessages());
                return Result.failure(mappingResult.notification());
            }

            IssueDocumentRequest request = mappingResult.orElseThrow();
            log.debug(
                    "Successfully mapped LoanTransaction to IssueDocumentRequest - items: {}",
                    request.getItems().size());

            Result<List<TransferMoneyResponse>> issueResult = issueGeneralDocument(request);
            if (issueResult.isFailure()) {
                return Result.failure(issueResult.notification());
            }

            List<TransferMoneyResponse> returns = issueResult.orElseThrow();

            Result<TransactionNumber> transactionNumberResult = extractTransactionNumber(returns);
            if (transactionNumberResult.isFailure()) {
                return Result.failure(transactionNumberResult.notification());
            }

            TransactionNumber transactionNumber = transactionNumberResult.orElseThrow();

            log.info(
                    "Document issued successfully - transactionNumber: {}, processed items: {}",
                    transactionNumber.value(),
                    returns.size());
            return Result.success(TrackedTransactionNumber.create(
                    transactionNumber.value(), trackingId.toString(), TransactionStatus.POSTED, clock));

        } catch (Exception e) {
            log.error("Unexpected error issuing document from LoanTransaction", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Failed to issue document: " + e.getMessage()));
        }
    }

    @Override
    public Result<List<TrackedTransactionNumber>> postTransactions(
            LoanFacilityId facilityId, String documentComment, List<LoanTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Result.success(List.of());
        }

        log.info("Batch posting {} transactions", transactions.size());

        Result<LoanTransaction> mergedTransactionResult =
                LoanTransactionMerger.merge(clock, facilityId, documentComment, transactions);

        if (mergedTransactionResult.isFailure()) {
            log.error(
                    "Failed to merge transactions: {}",
                    mergedTransactionResult.notification().getErrorMessages());
            return Result.failure(mergedTransactionResult.notification());
        }

        Result<TrackedTransactionNumber> postResult = postTransaction(mergedTransactionResult.orElseThrow());
        if (postResult.isFailure()) {
            return Result.failure(postResult.notification());
        }

        TrackedTransactionNumber sharedTrackedNumber = postResult.orElseThrow();

        return Result.success(List.of(sharedTrackedNumber));
    }

    @Override
    public Result<Void> reverseTransaction(TrackedTransactionNumber transactionNumber) {
        log.debug("Cancelling transfer money loan - {}", transactionNumber.value());

        List<Parameter> parameters = buildReverseTransactionsParameters(transactionNumber);

        Usecases usecases = requestBuilder.buildUseCase("cancel-transfer-money-loan", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<CancelTransferMoneyResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, CancelTransferMoneyResponse.class, FcbContext.empty());

        if (fcbResult.isFailure()) {
            log.debug(
                    "FCB cancel-transfer-money-loan failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        CancelTransferMoneyResponse response = fcbResult.orElseThrow();

        if (response.getTransactionNumber() == null) {
            log.debug(
                    "Transfer money cancellation completed but may not be fully successful - "
                            + "transactionId: {}, transactionNumber: {}",
                    response.getTransactionId(),
                    null);

            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_TRANSACTION_FAILED,
                    "Cancellation completed but verification failed"));
        }

        log.debug("Transfer money loan cancelled successfully - transactionNumber: {}", transactionNumber.value());

        return Result.success();
    }

    private Result<List<TransferMoneyResponse>> issueGeneralDocument(IssueDocumentRequest request) {
        log.debug(
                "Issuing general document - transactionId: {}, branchCode: {}, items count: {}",
                request.getTransactionId(),
                request.getBranchCode(),
                request.getItems() != null ? request.getItems().size() : 0);

        try {
            request.validate();

            List<Parameter> parameters = issueDocumentRequestBuilder.buildParameters(request);

            Usecases usecases = requestBuilder.buildUseCase("issue-general-document", parameters);
            FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

            log.debug("Executing FCB issue-general-document usecase");

            Result<IssueGeneralDocumentResponse> fcbResult =
                    fcbService.executeUsecase(fcbRequest, IssueGeneralDocumentResponse.class, FcbContext.empty());

            if (fcbResult.isFailure()) {
                log.error(
                        "FCB issue-general-document failed: {}",
                        fcbResult.notification().getErrorMessages());
                return Result.failure(fcbResult.notification());
            }

            IssueGeneralDocumentResponse response = fcbResult.orElseThrow();

            Result<List<TransferMoneyResponse>> validationResult = validateResponse(response);
            if (validationResult.isFailure()) {
                return validationResult;
            }

            return Result.success(response.getTransferMoneyReturns());

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error issuing document", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Failed to issue document: " + e.getMessage()));
        }
    }

    private Result<List<TransferMoneyResponse>> validateResponse(IssueGeneralDocumentResponse response) {
        Notification notification = Notification.create();

        if (response == null) {
            notification.addError(FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Response is null");
            return Result.failure(notification);
        }

        if (response.getTransferMoneyReturns() == null
                || response.getTransferMoneyReturns().isEmpty()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "No transfer money returns in response");
            return Result.failure(notification);
        }

        long failedCount = response.getFailedItemsCount();
        if (failedCount > 0) {
            log.warn(
                    "Document issued with {} failed items out of {}",
                    failedCount,
                    response.getTransferMoneyReturns().size());

            response.getTransferMoneyReturns().stream()
                    .filter(TransferMoneyResponse::isError)
                    .forEach(item -> log.error(
                            "Failed item - depositNumber: {}, error: {}",
                            item.getDepositNumber(),
                            item.getErrorMessage()));
        }

        return Result.success(response.getTransferMoneyReturns());
    }

    private Result<TransactionNumber> extractTransactionNumber(List<TransferMoneyResponse> returns) {
        List<TransferMoneyResponse> successfulResponses =
                returns.stream().filter(TransferMoneyResponse::isSuccess).toList();

        if (successfulResponses.isEmpty()) {
            log.warn("No successful TransferMoneyResponse found in the list.");
            return Result.failure(Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE));
        }

        if (successfulResponses.size() > 1) {
            log.warn("Multiple successful responses found ({}), expected only one", successfulResponses.size());
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_MULTIPLE_TRANSACTION_CODES));
        }

        TransferMoneyResponse successResponse = successfulResponses.getFirst();

        String transactionCode = successResponse.getTransactionCode();
        if (transactionCode != null && !transactionCode.isBlank()) {
            return TransactionNumber.of(transactionCode);
        }

        return Result.failure(Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_MISSING_TRANSACTION_CODE));
    }

    private List<Parameter> buildReverseTransactionsParameters(TrackedTransactionNumber transactionNumber) {
        List<Parameter> parameters = new ArrayList<>();

        parameters.add(Parameter.builder()
                .key("transactionNumber")
                .value(transactionNumber.value())
                .build());

        return parameters;
    }
}
