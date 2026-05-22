package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.AccountInfoKafkaResponse;
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
@Profile("kafka-fcb")
@RequiredArgsConstructor
public class FcbAccountKafkaAdapter implements AccountServicePort, FindOrCreateAccountPort, FindAccountByIdPort {

    private final FcbKafkaClient kafkaClient;
    private final FcbKafkaProperties properties;
    private final AuthenticationContextHolder authenticationContextHolder;

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

    private <T> Result<T> sendAndMap(
            FcbKafkaBaseRequest request,
            java.util.function.Function<AccountInfoKafkaResponse, Result<T>> responseMapper) {

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.getDefaultTimeout());
        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        FcbKafkaBaseResponse raw = result.unwrap();
        if (!(raw instanceof AccountInfoKafkaResponse response)) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, request.getOperationName()));
        }

        return responseMapper.apply(response);
    }
}
