package ir.dotin.loan.trade.adapters.driven.fcbmessaging.adapter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.AccountInfoKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.BatchCloseAccountKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.BatchOpenAccountKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaAccountMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByIdPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindOrCreateAccountPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcbAccountKafkaAdapter implements AccountServicePort, FindOrCreateAccountPort, FindAccountByIdPort {

    private final FcbRequestReplyClient kafkaClient;
    private final AuthenticationContextHolder authenticationContextHolder;

    /**
     * Per-call request/reply timeout, bound from Consul KV {@code nova.fcb.kafka.default-timeout} (defaults to 10s).
     */
    @Value("${nova.fcb.kafka.default-timeout:10s}")
    private Duration defaultTimeout;

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "openAccount"})
    public Result<AccountInfo> openAccount(LoanTopic loanTopic, String currencyCode) {
        var branchOpt = authenticationContextHolder.branchCode();
        if (branchOpt.isEmpty()) {
            return Result.failure(CoreBankingErrors.BRANCH_CODE_MISSING);
        }
        String branchCode = branchOpt.get();
        String idempotencyKey = UUID.randomUUID().toString();

        return sendAndMap(
                OpenAccountByTopicRequest.builder()
                        .title(loanTopic.name())
                        .topicCode(loanTopic.code())
                        .branchCode(branchCode)
                        .currencyCode(currencyCode)
                        .transactionId(idempotencyKey)
                        .build(),
                response -> KafkaAccountMapper.mapToAccountInfoFromOpenAccount(response, loanTopic));
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "openAccount"})
    public Result<AccountId> openAccount(CreateAccountInfo info) {
        var request = OpenAccountRequest.builder()
                .transactionId(info.transactionId())
                .topic(info.topicCode())
                .swiftCode(info.currencyType().getCode())
                .branchCode(info.branchCode().value())
                .createAccountGroup(Boolean.TRUE.equals(info.createAccountGroup()))
                .newAccBranchCode(info.newAccBranchCode().value())
                .newAccAccountNumber(info.newAccAccountId().value())
                .newAccTitle(info.newAccTitle().value())
                .newAccAmount(info.newAccAmount().value())
                .newAccMinAmount(info.newAccMinAmount().value())
                .newAccMaxAmount(info.newAccMaxAmount().value())
                .newAccBaseCurrencyAmount(info.newAccBaseCurrencyAmount().value())
                .newAccDebtorAmount(info.newAccDebtorAmount().value())
                .newAccCreditorAmount(info.newAccCreditorAmount().value())
                .build();

        return sendAndMap(request, KafkaAccountMapper::mapToAccountId);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "deleteAccount"})
    public Result<AccountNumber> deleteAccount(UUID transactionId, UUID rollBackId, AccountNumber accountNumber) {
        var request = DeleteAccountRequest.builder()
                .accountNumber(accountNumber != null ? accountNumber.accountNumber() : null)
                .transactionId(transactionId.toString())
                .rollBackId(rollBackId != null ? rollBackId.toString() : null)
                .build();

        return sendAndMap(request, KafkaAccountMapper::mapToDeletedAccountNumber);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "validateAccountNumber"})
    public Result<AccountNumber> validateAccountNumber(String accountNumber) {
        return sendAndMap(
                ValidateAccountNumberRequest.builder()
                        .accountNumber(accountNumber)
                        .build(),
                KafkaAccountMapper::mapToValidatedAccountNumber);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "findOrCreateAccount"})
    public Result<AccountInfo> findOrCreateAccount(LoanTopic loanTopic) {
        return sendAndMap(
                FindOrCreateAccountRequest.builder()
                        .title(loanTopic.name())
                        .topicCode(loanTopic.code())
                        .build(),
                response -> KafkaAccountMapper.mapToFindOrCreateAccountInfo(response, loanTopic));
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "findAccountById"})
    public Result<AccountInfo> findAccountById(AccountId accountId) {
        log.debug("findAccountById called for accountId={}   not yet implemented via Kafka", accountId.value());
        return Result.failure(ir.dotin.platform.pangaea.commons.core.Notification.ofError(
                CoreBankingErrors.KAFKA_INVALID_RESPONSE, "findAccountById-not-implemented"));
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "openAccounts"})
    public Result<List<AccountInfo>> openAccounts(List<LoanTopic> loanTopics, String currencyCode) {
        if (loanTopics == null || loanTopics.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        var branchOpt = authenticationContextHolder.branchCode();
        if (branchOpt.isEmpty()) {
            return Result.failure(CoreBankingErrors.BRANCH_CODE_MISSING);
        }
        String branchCode = branchOpt.get();

        Map<String, LoanTopic> topicByTxId = new LinkedHashMap<>();
        List<BatchOpenAccountRequest.Item> items = new ArrayList<>();
        for (LoanTopic topic : loanTopics) {
            String transactionId = UUID.randomUUID().toString();
            topicByTxId.put(transactionId, topic);
            items.add(BatchOpenAccountRequest.Item.builder()
                    .title(topic.name())
                    .topicCode(topic.code())
                    .branchCode(branchCode)
                    .currencyCode(currencyCode)
                    .transactionId(transactionId)
                    .build());
        }

        return sendTyped(
                BatchOpenAccountRequest.builder().items(items).build(),
                BatchOpenAccountKafkaResponse.class,
                response -> mapBatchOpen(response, topicByTxId));
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "closeAccounts"})
    public Result<List<AccountNumber>> closeAccounts(List<AccountNumber> accountNumbers) {
        if (accountNumbers == null || accountNumbers.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<BatchCloseAccountRequest.Item> items = new ArrayList<>();
        for (AccountNumber accountNumber : accountNumbers) {
            items.add(BatchCloseAccountRequest.Item.builder()
                    .accountNumber(accountNumber.accountNumber())
                    .transactionId(UUID.randomUUID().toString())
                    .build());
        }

        return sendTyped(
                BatchCloseAccountRequest.builder().items(items).build(),
                BatchCloseAccountKafkaResponse.class,
                this::mapBatchClose);
    }

    private Result<List<AccountInfo>> mapBatchOpen(
            BatchOpenAccountKafkaResponse response, Map<String, LoanTopic> topicByTxId) {
        @Nullable List<BatchOpenAccountKafkaResponse.Item> results = response.getResults();
        if (results == null || results.size() != topicByTxId.size()) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "nova-batch-open-accounts"));
        }
        Map<String, String> accountByTxId = new HashMap<>();
        for (BatchOpenAccountKafkaResponse.Item item : results) {
            @Nullable String transactionId = item.getTransactionId();
            @Nullable String accountNumber = item.getAccountNumber();
            if (transactionId != null && accountNumber != null) {
                accountByTxId.put(transactionId, accountNumber);
            }
        }

        List<AccountInfo> infos = new ArrayList<>();
        for (Map.Entry<String, LoanTopic> entry : topicByTxId.entrySet()) {
            @Nullable String accountNumber = accountByTxId.get(entry.getKey());
            if (accountNumber == null || accountNumber.isBlank()) {
                return Result.failure(
                        Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "nova-batch-open-accounts"));
            }
            Result<AccountId> accountIdResult = AccountId.valueOf(accountNumber);
            if (accountIdResult.isFailure()) {
                return Result.failure(accountIdResult.err().orElseThrow());
            }
            Result<AccountInfo> infoResult = AccountInfo.of(accountIdResult.unwrap(), entry.getValue());
            if (infoResult.isFailure()) {
                return Result.failure(infoResult.err().orElseThrow());
            }
            infos.add(infoResult.unwrap());
        }
        return Result.success(infos);
    }

    private Result<List<AccountNumber>> mapBatchClose(BatchCloseAccountKafkaResponse response) {
        @Nullable List<BatchCloseAccountKafkaResponse.Item> results = response.getResults();
        if (results == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "nova-batch-close-accounts"));
        }
        List<AccountNumber> closed = new ArrayList<>();
        for (BatchCloseAccountKafkaResponse.Item item : results) {
            @Nullable String accountNumber = item.getAccountNumber();
            if (accountNumber == null || accountNumber.isBlank()) {
                continue;
            }
            Result<AccountNumber> accountNumberResult = AccountNumber.of(accountNumber);
            if (accountNumberResult.isFailure()) {
                return Result.failure(accountNumberResult.err().orElseThrow());
            }
            closed.add(accountNumberResult.unwrap());
        }
        return Result.success(closed);
    }

    private <T> Result<T> sendAndMap(
            FcbKafkaBaseRequest request, Function<AccountInfoKafkaResponse, Result<T>> responseMapper) {
        return sendTyped(request, AccountInfoKafkaResponse.class, responseMapper);
    }

    private <R extends FcbKafkaBaseResponse, T> Result<T> sendTyped(
            FcbKafkaBaseRequest request, Class<R> responseType, Function<R, Result<T>> responseMapper) {

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, defaultTimeout);
        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        FcbKafkaBaseResponse raw = result.unwrap();
        if (!responseType.isInstance(raw)) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, request.getOperationName()));
        }

        return responseMapper.apply(responseType.cast(raw));
    }
}
