package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.security.AuthenticationContextHolder;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response.AccountInfoKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaAccountMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByIdPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindOrCreateAccountPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;

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

    // ── AccountServicePort ──

    @Override
    public Result<AccountInfo> openAccount(LoanTopic loanTopic, String currencyCode) {
        String branchCode = authenticationContextHolder.branchCode().orElseThrow();
        String idempotencyKey = UUID.randomUUID().toString();
        return sendAndMap(
                new OpenAccountByTopicRequest(
                        loanTopic.name(), loanTopic.code(), branchCode, currencyCode, idempotencyKey),
                response -> KafkaAccountMapper.mapToAccountInfoFromOpenAccount(response, loanTopic));
    }

    @Override
    public Result<AccountId> openAccount(CreateAccountInfo info) {
        var request = new OpenAccountRequest(
                info.transactionId(),
                info.topicCode(),
                info.currencyType().getCode(),
                info.branchCode().value(),
                Boolean.TRUE.equals(info.createAccountGroup()),
                info.newAccBranchCode().value(),
                info.newAccAccountId().value(),
                info.newAccTitle().value(),
                info.newAccAmount().value(),
                info.newAccMinAmount().value(),
                info.newAccMaxAmount().value(),
                info.newAccBaseCurrencyAmount().value(),
                info.newAccDebtorAmount().value(),
                info.newAccCreditorAmount().value());
        return sendAndMap(request, KafkaAccountMapper::mapToAccountId);
    }

    @Override
    public Result<AccountNumber> deleteAccount(UUID transactionId, UUID rollBackId, AccountNumber accountNumber) {
        var request = new DeleteAccountRequest(
                accountNumber != null ? accountNumber.accountNumber() : null,
                transactionId.toString(),
                rollBackId != null ? rollBackId.toString() : null);
        return sendAndMap(request, KafkaAccountMapper::mapToDeletedAccountNumber);
    }

    @Override
    public Result<AccountNumber> validateAccountNumber(String accountNumber) {
        return sendAndMap(
                new ValidateAccountNumberRequest(accountNumber), KafkaAccountMapper::mapToValidatedAccountNumber);
    }

    // ── FindOrCreateAccountPort ──

    @Override
    public Result<AccountInfo> findOrCreateAccount(LoanTopic loanTopic) {
        return sendAndMap(
                new FindOrCreateAccountRequest(loanTopic.name(), loanTopic.code()),
                response -> KafkaAccountMapper.mapToFindOrCreateAccountInfo(response, loanTopic));
    }

    // ── FindAccountByIdPort ──

    @Override
    public Result<AccountInfo> findAccountById(AccountId accountId) {
        log.debug("findAccountById called for accountId={} — not yet implemented via Kafka", accountId.value());
        return Result.success();
    }

    // ── Internal helpers ──

    private <T> Result<T> sendAndMap(
            FcbKafkaBaseRequest request,
            java.util.function.Function<AccountInfoKafkaResponse, Result<T>> responseMapper) {

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.defaultTimeout());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbKafkaBaseResponse raw = result.orElseThrow();
        if (!(raw instanceof AccountInfoKafkaResponse response)) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, request.getOperationName()));
        }
        return responseMapper.apply(response);
    }
}
