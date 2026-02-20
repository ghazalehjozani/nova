package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbMessagingProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsParameter;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbJmsLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.JmsAccountMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByIdPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindOrCreateAccountPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("activemq")
public class FcbAccountJmsAdapter implements AccountServicePort, FindOrCreateAccountPort, FindAccountByIdPort {

    private final FcbJmsClient jmsClient;
    private final JmsTemplate jmsTemplate;
    private final FcbMessagingProperties properties;

    public FcbAccountJmsAdapter(
            FcbJmsClient jmsClient,
            @Qualifier("fcbAccountJmsTemplate") JmsTemplate jmsTemplate,
            FcbMessagingProperties properties) {
        this.jmsClient = jmsClient;
        this.jmsTemplate = jmsTemplate;
        this.properties = properties;
    }

    // ── AccountServicePort ──

    @Override
    public Result<AccountInfo> openAccount(LoanTopic loanTopic) {
        String branchCode = "1"; // TODO: resolve from security context
        return sendAndMap(
                "electronic-bill-create-account",
                JmsAccountMapper.mapOpenAccountByTopicParams(loanTopic, branchCode),
                response -> JmsAccountMapper.mapToAccountInfoFromOpenAccount(response.payload(), loanTopic));
    }

    @Override
    public Result<AccountId> openAccount(CreateAccountInfo createAccountInfo) {
        return sendAndMap(
                "nova-open-account",
                JmsAccountMapper.mapOpenAccountParams(createAccountInfo),
                response -> JmsAccountMapper.mapToAccountId(response.payload()));
    }

    @Override
    public Result<AccountNumber> deleteAccount(UUID transactionId, UUID rollBackId, AccountNumber accountNumber) {
        return sendAndMap(
                "nova-delete-account",
                JmsAccountMapper.mapDeleteAccountParams(transactionId, rollBackId, accountNumber),
                response -> JmsAccountMapper.mapToDeletedAccountNumber(response.payload()));
    }

    @Override
    public Result<AccountNumber> validateAccountNumber(String accountNumber) {
        return sendAndMap(
                "load-account-by-account-number-service",
                JmsAccountMapper.mapValidateAccountNumberParams(accountNumber),
                response -> JmsAccountMapper.mapToValidatedAccountNumber(response.payload()));
    }

    // ── FindOrCreateAccountPort ──

    @Override
    public Result<AccountInfo> findOrCreateAccount(LoanTopic loanTopic) {
        return sendAndMap(
                "find-or-create-account",
                JmsAccountMapper.mapFindOrCreateAccountParams(loanTopic),
                response -> JmsAccountMapper.mapToFindOrCreateAccountInfo(response.payload(), loanTopic));
    }

    // ── FindAccountByIdPort ──

    @Override
    public Result<AccountInfo> findAccountById(AccountId accountId) {
        log.debug("findAccountById called for accountId={} — not yet implemented via JMS", accountId.value());
        return Result.success();
    }

    // ── Internal helpers ──

    private <T> Result<T> sendAndMap(
            String operationName,
            List<FcbJmsParameter> parameters,
            java.util.function.Function<FcbJmsResponse, Result<T>> responseMapper) {

        FcbJmsRequest request = new FcbJmsRequest(UUID.randomUUID().toString(), operationName, parameters, null);
        Result<FcbJmsResponse> result = jmsClient.sendAndReceive(jmsTemplate, request, properties.account());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbJmsResponse response = result.orElseThrow();
        if (response.payload() == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, operationName));
        }
        return responseMapper.apply(response);
    }
}
