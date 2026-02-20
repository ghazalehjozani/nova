package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsParameter;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbJmsLocalizedMessageCodes;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CreateAccountInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps domain objects to/from JMS parameters for the account queue. Covers: AccountServicePort,
 * FindOrCreateAccountPort, FindAccountByIdPort.
 */
@Slf4j
@UtilityClass
public class JmsAccountMapper {

    // ── AccountServicePort: openAccount(LoanTopic) ──

    public List<FcbJmsParameter> mapOpenAccountByTopicParams(LoanTopic loanTopic, String branchCode) {
        return List.of(
                new FcbJmsParameter("title", loanTopic.name()),
                new FcbJmsParameter("topicCode", loanTopic.code()),
                new FcbJmsParameter("branchCode", branchCode));
    }

    public Result<AccountInfo> mapToAccountInfoFromOpenAccount(Map<String, Object> payload, LoanTopic loanTopic) {
        String accountNumber = asString(payload.get("accountNumber"));
        if (accountNumber == null || accountNumber.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "openAccount"));
        }
        Result<AccountId> accountIdResult = AccountId.valueOf(accountNumber);
        if (accountIdResult.isFailure()) {
            return Result.failure(accountIdResult.notification());
        }
        return AccountInfo.of(accountIdResult.orElseThrow(), loanTopic);
    }

    // ── AccountServicePort: openAccount(CreateAccountInfo) ──

    public List<FcbJmsParameter> mapOpenAccountParams(CreateAccountInfo info) {
        List<FcbJmsParameter> params = new ArrayList<>();
        params.add(new FcbJmsParameter("transactionId", info.transactionId()));
        params.add(new FcbJmsParameter("topic", info.topicCode()));
        params.add(new FcbJmsParameter("swiftCode", info.currencyType().getCode()));
        params.add(new FcbJmsParameter("branchCode", info.branchCode().value()));
        params.add(new FcbJmsParameter(
                "createAccountGroup", Boolean.TRUE.equals(info.createAccountGroup()) ? "true" : "false"));
        params.add(
                new FcbJmsParameter("newAccBranchCode", info.newAccBranchCode().value()));
        params.add(new FcbJmsParameter(
                "newAccAccountNumber", info.newAccAccountId().value()));
        params.add(new FcbJmsParameter("newAccTitle", info.newAccTitle().value()));
        params.add(
                new FcbJmsParameter("newAccAmount", info.newAccAmount().value().toString()));
        params.add(new FcbJmsParameter(
                "newAccMinAmount", info.newAccMinAmount().value().toString()));
        params.add(new FcbJmsParameter(
                "newAccMaxAmount", info.newAccMaxAmount().value().toString()));
        params.add(new FcbJmsParameter(
                "newAccBaseCurrencyAmount",
                info.newAccBaseCurrencyAmount().value().toString()));
        params.add(new FcbJmsParameter(
                "newAccDebtorAmount", info.newAccDebtorAmount().value().toString()));
        params.add(new FcbJmsParameter(
                "newAccCreditorAmount", info.newAccCreditorAmount().value().toString()));
        return params;
    }

    public Result<AccountId> mapToAccountId(Map<String, Object> payload) {
        String accountNumber = asString(payload.get("accountNumber"));
        if (accountNumber == null || accountNumber.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "nova-open-account"));
        }
        return AccountId.valueOf(accountNumber);
    }

    // ── AccountServicePort: deleteAccount ──

    public List<FcbJmsParameter> mapDeleteAccountParams(
            java.util.UUID transactionId, java.util.UUID rollBackId, AccountNumber accountNumber) {
        List<FcbJmsParameter> params = new ArrayList<>();
        if (accountNumber != null) {
            params.add(new FcbJmsParameter("accountNumber", accountNumber.accountNumber()));
        }
        if (rollBackId != null) {
            params.add(new FcbJmsParameter("rollBackId", String.valueOf(rollBackId)));
        }
        params.add(new FcbJmsParameter("transactionId", String.valueOf(transactionId)));
        return params;
    }

    public Result<AccountNumber> mapToDeletedAccountNumber(Map<String, Object> payload) {
        String accountNumber = asString(payload.get("accountNumber"));
        if (accountNumber == null || accountNumber.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "deleteAccount"));
        }
        return AccountNumber.of(accountNumber);
    }

    // ── AccountServicePort: validateAccountNumber ──

    public List<FcbJmsParameter> mapValidateAccountNumberParams(String accountNumber) {
        return List.of(new FcbJmsParameter("accountNumber", accountNumber));
    }

    public Result<AccountNumber> mapToValidatedAccountNumber(Map<String, Object> payload) {
        String accountNumber = asString(payload.get("accountNumber"));
        String id = asString(payload.get("id"));
        if (id == null || accountNumber == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "validateAccountNumber"));
        }
        return AccountNumber.of(accountNumber);
    }

    // ── FindOrCreateAccountPort ──

    public List<FcbJmsParameter> mapFindOrCreateAccountParams(LoanTopic loanTopic) {
        return List.of(
                new FcbJmsParameter("title", loanTopic.name()), new FcbJmsParameter("topicCode", loanTopic.code()));
    }

    public Result<AccountInfo> mapToFindOrCreateAccountInfo(Map<String, Object> payload, LoanTopic loanTopic) {
        String accountNumber = asString(payload.get("accountNumber"));
        if (accountNumber == null || accountNumber.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "findOrCreateAccount"));
        }
        Result<AccountId> accountIdResult = AccountId.valueOf(accountNumber);
        if (accountIdResult.isFailure()) {
            return Result.failure(accountIdResult.notification());
        }
        return AccountInfo.of(accountIdResult.orElseThrow(), loanTopic);
    }

    // ── Helper ──

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }
}
