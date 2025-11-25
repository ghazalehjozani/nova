package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.security.AuthenticationContextHolder;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.FailureReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.ExtraInfoVO;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CancelTransferMoneyResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.OpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.CancelTransferMoneyMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceAdapter implements AccountServicePort {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;
    private final AuthenticationContextHolder authenticationContextHolder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result<AccountInfo> openAccount(LoanTopic loanTopic) {
        String branchCode = authenticationContextHolder.branchCode().orElseThrow(); // TODO: add custom exception
        log.debug(
                "Opening account with title: {}, topicCode: {}, branchCode: {}",
                loanTopic.name(),
                loanTopic.code(),
                branchCode);

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
                        .value(branchCode)
                        .build());

        Usecases usecases = requestBuilder.buildUseCase("electronic-bill-create-account", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<OpenAccountResponse> openAccountResponseResult =
                fcbService.executeUsecase(fcbRequest, OpenAccountResponse.class, FcbContext.empty());

        if (Objects.isNull(openAccountResponseResult.value())
                || Objects.isNull(openAccountResponseResult.value().getAccountNumber())) {
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.ACCOUNT_NUMBER_NOT_FOUND, loanTopic.code()));
        }
        return AccountInfo.of(
                AccountId.valueOf(openAccountResponseResult.value().getAccountNumber())
                        .getValue(),
                loanTopic);
    }

    @Override
    public Result<TransactionNumber> cancelTransferMoney(
            String transactionId,
            TransactionNumber transactionNumber,
            List<Article> articles,
            BranchCode branchCode,
            FailureReason failureReason) {
        log.info(
                "Cancelling transfer money loan - {}",
                CancelTransferMoneyMapper.createCancellationSummary(transactionNumber, articles));

        try {

            Result<ExtraInfoVO> extraInfoResult = CancelTransferMoneyMapper.mapToExtraInfo(
                    transactionId, transactionNumber, articles, branchCode, failureReason);
            if (extraInfoResult.isFailure()) {
                log.error(
                        "Failed to map articles to ExtraInfo: {}",
                        extraInfoResult.notification().getErrorMessages());
                return Result.failure(extraInfoResult.notification());
            }

            ExtraInfoVO extraInfo = extraInfoResult.orElseThrow();
            log.debug("Mapped {} articles to ExtraInfo successfully", articles.size());

            List<Parameter> parameters = buildParameters(transactionId, transactionNumber, extraInfo);

            Usecases usecases = requestBuilder.buildUseCase("cancel-transfer-money-loan", parameters);
            FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

            Result<CancelTransferMoneyResponse> fcbResult =
                    fcbService.executeUsecase(fcbRequest, CancelTransferMoneyResponse.class, FcbContext.empty());

            if (fcbResult.isFailure()) {
                log.error(
                        "FCB cancel-transfer-money-loan failed: {}",
                        fcbResult.notification().getErrorMessages());
                return Result.failure(fcbResult.notification());
            }

            CancelTransferMoneyResponse response = fcbResult.orElseThrow();

            if (response.getTransactionNumber() == null) {
                log.warn(
                        "Transfer money cancellation completed but may not be fully successful - "
                                + "transactionId: {}, transactionNumber: {}",
                        response.getTransactionId(),
                        null);

                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_TRANSACTION_FAILED,
                        "Cancellation completed but verification failed"));
            }

            log.info(
                    "Transfer money loan cancelled successfully - transactionId: {}, transactionNumber: {}",
                    transactionId,
                    transactionNumber.value());

            TransactionNumber cancelationTransactionNumber = new TransactionNumber(response.getTransactionNumber());

            return Result.success(cancelationTransactionNumber);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error cancelling transfer money loan", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR,
                    "Failed to cancel transfer money loan: " + e.getMessage()));
        }
    }

    private List<Parameter> buildParameters(
            String transactionId, TransactionNumber transactionNumber, ExtraInfoVO extraInfo) {
        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
                Parameter.builder().key("transactionId").value(transactionId).build());

        parameters.add(Parameter.builder()
                .key("transactionNumber")
                .value(transactionNumber.value())
                .build());

        String extraInfoJson = convertExtraInfoToEscapedJson(extraInfo);
        parameters.add(Parameter.builder().key("extraInfo").value(extraInfoJson).build());

        log.debug("Built {} parameters for cancel-transfer-money-loan", parameters.size());
        log.debug(
                "Parameters - transactionId: {}, transactionNumber: {}, extraInfo length: {} chars",
                transactionId,
                transactionNumber.value(),
                extraInfoJson.length());

        return parameters;
    }

    private String convertExtraInfoToEscapedJson(ExtraInfoVO extraInfo) {
        try {
            String json = objectMapper.writeValueAsString(extraInfo);
            String escaped = json.replace("\"", "&quot;");
            log.debug("Converted ExtraInfo to JSON: {} chars", escaped.length());
            return escaped;
        } catch (JsonProcessingException e) {
            log.error("Error converting ExtraInfoVO to JSON", e);
            return "{}";
        }
    }
}
