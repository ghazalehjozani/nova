package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.IssueDocumentRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.IssueGeneralDocumentResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.OpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.TransferMoneyResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.AccountMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.IssueDocumentRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceAdapter implements AccountServicePort {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;
    private final IssueDocumentRequestBuilder issueDocumentRequestBuilder;

    @Override
    public Result<AccountId> openAccount(LoanTopic loanTopic, BranchCode branchCode) {
        log.debug(
                "Opening account with title: {}, topicCode: {}, branchCode: {}",
                loanTopic.name(),
                loanTopic.code(),
                branchCode.value());

        List<Parameter> parameters = Arrays.asList(
                Parameter.builder()
                        .type("constant")
                        .key("title")
                        .value(loanTopic.name())
                        .build(),
                Parameter.builder()
                        .type("constant")
                        .key("topicCode")
                        .value(loanTopic.code())
                        .build(),
                Parameter.builder()
                        .type("constant")
                        .key("branchCode")
                        .value(branchCode.value())
                        .build());

        Usecases usecases = requestBuilder.buildUseCase("electronic-bill-create-account", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<OpenAccountResponse> openAccountResponseResult =
                fcbService.executeUsecase(fcbRequest, OpenAccountResponse.class);

        if (Objects.isNull(openAccountResponseResult.value())
                || Objects.isNull(openAccountResponseResult.value().getAccountNumber())) {
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.ACCOUNT_NUMBER_NOT_FOUND,
                    openAccountResponseResult.value().getAccountNumber()));
        }
        return AccountId.valueOf(openAccountResponseResult.value().getAccountNumber());
    }

    @Override
    public Result<TransactionNumber> postTransaction(LoanTransaction loanTransaction) {
        log.info(
                "Issuing document from LoanTransaction - facilityId: {}, articles: {}",
                loanTransaction.loanFacilityId().value(),
                loanTransaction.document().articles().size());

        try {
            Result<IssueDocumentRequest> mappingResult = AccountMapper.mapToIssueDocumentRequest(loanTransaction);
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

            Result<Void> validationResult = AccountMapper.validateTransferMoneyReturns(returns);
            if (validationResult.isFailure()) {
                return Result.failure(validationResult.notification());
            }

            TransactionNumber transactionNumber = extractTransactionNumber(returns);

            log.info(
                    "Document issued successfully - transactionNumber: {}, processed items: {}",
                    transactionNumber.value(),
                    returns.size());

            return Result.success(transactionNumber);

        } catch (Exception e) {
            log.error("Unexpected error issuing document from LoanTransaction", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR, "Failed to issue document: " + e.getMessage()));
        }
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
                    fcbService.executeUsecase(fcbRequest, IssueGeneralDocumentResponse.class);

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

            log.info(
                    "FCB document issued - documentNumber: {}, successful items: {}/{}",
                    response.getDocumentNumber(),
                    response.getSuccessfulItemsCount(),
                    response.getTransferMoneyReturns() != null
                            ? response.getTransferMoneyReturns().size()
                            : 0);

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

    private TransactionNumber extractTransactionNumber(List<TransferMoneyResponse> returns) {
        String documentNumber = returns.stream()
                .filter(TransferMoneyResponse::isSuccess)
                .map(TransferMoneyResponse::getIdentifier)
                .filter(num -> num != null && !num.isBlank())
                .findFirst()
                .orElse("");

        Result<TransactionNumber> result = TransactionNumber.of(documentNumber);
        if (result.isFailure()) {
            log.warn("Failed to create TransactionNumber from '{}', using fallback", documentNumber);
            return TransactionNumber.of("FCB-" + System.currentTimeMillis()).orElseThrow();
        }

        return result.orElseThrow();
    }
}
