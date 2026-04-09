package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.*;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.security.api.AuthenticationContextHolder;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.AccountInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DeleteAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.LoadAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.OpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.LoanMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!kafka-fcb")
@RequiredArgsConstructor
public class AccountServiceAdapter implements AccountServicePort {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;
    private final AuthenticationContextHolder authenticationContextHolder;

    @Override
    public Result<AccountInfo> openAccount(LoanTopic loanTopic, String currencyCode) {
        //        String branchCode = authenticationContextHolder.branchCode().orElseThrow(); // TODO: add custom
        // exception
        String branchCode = "1";
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
    public Result<AccountNumber> deleteAccount(UUID transactionId, UUID rollBackId, AccountNumber accountNumber) {
        try {
            List<Parameter> parameters = new ArrayList<>();

            if (Objects.nonNull(accountNumber)) {
                parameters.add(Parameter.builder()
                        .type("constant")
                        .key("accountNumber")
                        .value(accountNumber.accountNumber())
                        .build());
            }
            if (Objects.nonNull(rollBackId)) {
                parameters.add(Parameter.builder()
                        .type("constant")
                        .key("rollBackId")
                        .value(String.valueOf(rollBackId))
                        .build());
            }
            parameters.add(Parameter.builder()
                    .type("constant")
                    .key("transactionId")
                    .value(String.valueOf(transactionId))
                    .build());

            Usecases usecases = requestBuilder.buildUseCase("nova-delete-account", parameters);
            FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();
            Map<String, Object> additionalContext = new HashMap<>();
            if (accountNumber != null) {
                additionalContext.put("accountNumber", accountNumber.accountNumber());
            }
            if (rollBackId != null) {
                additionalContext.put("rollBackId", rollBackId);
            }
            FcbContext fcbContext =
                    FcbContext.builder().additionalContext(additionalContext).build();
            Result<DeleteAccountResponse> fcbResult =
                    fcbService.executeUsecase(fcbRequest, DeleteAccountResponse.class, fcbContext);

            if (fcbResult.isFailure()) {
                return Result.failure(fcbResult.notification());
            }

            DeleteAccountResponse response = fcbResult.orElseThrow();

            AccountNumber account = new AccountNumber(response.getAccountNumber());

            return Result.success(account);

        } catch (IllegalArgumentException e) {
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, e.getMessage()));
        } catch (Exception e) {
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_UNKNOWN_ERROR,
                    "Failed to cancel transfer money loan: " + e.getMessage()));
        }
    }

    @Override
    public Result<AccountId> openAccount(CreateAccountInfo createAccountInfo) {
        log.info("opening account by calling nova-open-account: {}", createAccountInfo);

        List<Parameter> parameters = buildOpenAccountParameters(createAccountInfo);

        Usecases usecases = requestBuilder.buildUseCase("nova-open-account", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Map<String, Object> additionalContext = new HashMap<>();
        // todo
        //        additionalContext.put("topic", fcbRequest);
        //        additionalContext.put("newAccAmount", createAccountInfo.newAccAmount());
        //        additionalContext.put("newAccMinAmount", createAccountInfo.newAccMinAmount());
        //        additionalContext.put("newAccMaxAmount", createAccountInfo.newAccMaxAmount());
        //        additionalContext.put("newAccBaseCurrencyAmount", createAccountInfo.newAccBaseCurrencyAmount());
        //        additionalContext.put("newAccDebtorAmount", createAccountInfo.newAccDebtorAmount());
        //        additionalContext.put("newAccCreditorAmount", createAccountInfo.newAccCreditorAmount());
        //        additionalContext.put("createAccountGroup", createAccountInfo.createAccountGroup());

        log.debug("Executing FCB nova-open-account use case");
        Result<AccountInfoResponse> fcbResult = fcbService.executeUsecase(
                fcbRequest,
                AccountInfoResponse.class,
                FcbContext.builder().additionalContext(additionalContext).build());

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB nova-open-account failed: {}", fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        Result<AccountId> mapDtoResult = LoanMapper.mapToCreateAccountResult(fcbResult);
        if (mapDtoResult.isFailure()) {
            log.error(
                    "map nova-open-account failed: {}",
                    mapDtoResult.notification().getErrorMessages());
            return Result.failure(mapDtoResult.notification());
        }
        return mapDtoResult;
    }

    private List<Parameter> buildOpenAccountParameters(CreateAccountInfo createAccountInfo) {
        List<Parameter> parameters = new ArrayList<>();

        parameters.add(Parameter.builder()
                .key("transactionId")
                .value(createAccountInfo.transactionId())
                .build());

        parameters.add(Parameter.builder()
                .key("topic")
                .value(createAccountInfo.topicCode())
                .build());

        parameters.add(Parameter.builder()
                .key("swiftCode")
                .value(createAccountInfo.currencyType().getCode())
                .build());

        parameters.add(Parameter.builder()
                .key("branchCode")
                .value(createAccountInfo.branchCode().value())
                .build());

        parameters.add(Parameter.builder()
                .key("createAccountGroup")
                .value(Boolean.TRUE.equals(createAccountInfo.createAccountGroup()) ? "true" : "false")
                .build());

        parameters.add(Parameter.builder()
                .key("newAccBranchCode")
                .value(createAccountInfo.newAccBranchCode().value())
                .build());

        parameters.add(Parameter.builder()
                .key("newAccAccountNumber")
                .value(createAccountInfo.newAccAccountId().value())
                .build());

        parameters.add(Parameter.builder()
                .key("newAccTitle")
                .value(createAccountInfo.newAccTitle().value())
                .build());

        parameters.add(Parameter.builder()
                .key("newAccAmount")
                .value(createAccountInfo.newAccAmount().value().toString())
                .build());

        parameters.add(Parameter.builder()
                .key("newAccMinAmount")
                .value(createAccountInfo.newAccMinAmount().value().toString())
                .build());

        parameters.add(Parameter.builder()
                .key("newAccMaxAmount")
                .value(createAccountInfo.newAccMaxAmount().value().toString())
                .build());

        parameters.add(Parameter.builder()
                .key("newAccBaseCurrencyAmount")
                .value(createAccountInfo.newAccBaseCurrencyAmount().value().toString())
                .build());

        parameters.add(Parameter.builder()
                .key("newAccDebtorAmount")
                .value(createAccountInfo.newAccDebtorAmount().value().toString())
                .build());

        parameters.add(Parameter.builder()
                .key("newAccCreditorAmount")
                .value(createAccountInfo.newAccCreditorAmount().value().toString())
                .build());

        return parameters;
    }

    @Override
    public Result<AccountNumber> validateAccountNumber(String accountNumber) {
        log.info("Validating account number: {}", accountNumber);

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder()
                .key("accountNumber")
                .value(accountNumber)
                .type("constant")
                .build());

        Usecases usecases = requestBuilder.buildUseCase("load-account-by-account-number-service", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<LoadAccountResponse> result =
                fcbService.executeUsecase(fcbRequest, LoadAccountResponse.class, FcbContext.empty());

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        LoadAccountResponse response = result.value();
        if (response == null || response.getId() == null) {
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.ACCOUNT_NUMBER_NOT_FOUND, accountNumber));
        }

        AccountNumber validAccountNumber =
                AccountNumber.of(response.getAccountNumber()).orElseThrow();

        return Result.success(validAccountNumber);
    }
}
