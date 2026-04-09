package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response.AccountInfoKafkaResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaAccountMapper {

    public Result<AccountInfo> mapToAccountInfoFromOpenAccount(AccountInfoKafkaResponse response, LoanTopic loanTopic) {
        if (response.getAccountNumber() == null || response.getAccountNumber().isBlank()) {
            return Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "openAccount"));
        }
        Result<AccountId> accountIdResult = AccountId.valueOf(response.getAccountNumber());
        if (accountIdResult.isFailure()) {
            return Result.failure(accountIdResult.notification());
        }
        return AccountInfo.of(accountIdResult.orElseThrow(), loanTopic);
    }

    public Result<AccountId> mapToAccountId(AccountInfoKafkaResponse response) {
        if (response.getAccountNumber() == null || response.getAccountNumber().isBlank()) {
            return Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "nova-open-account"));
        }
        return AccountId.valueOf(response.getAccountNumber());
    }

    public Result<AccountNumber> mapToDeletedAccountNumber(AccountInfoKafkaResponse response) {
        if (response.getAccountNumber() == null || response.getAccountNumber().isBlank()) {
            return Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "deleteAccount"));
        }
        return AccountNumber.of(response.getAccountNumber());
    }

    public Result<AccountNumber> mapToValidatedAccountNumber(AccountInfoKafkaResponse response) {
        if (response.getId() == null || response.getAccountNumber() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "validateAccountNumber"));
        }
        return AccountNumber.of(response.getAccountNumber());
    }

    public Result<AccountInfo> mapToFindOrCreateAccountInfo(AccountInfoKafkaResponse response, LoanTopic loanTopic) {
        if (response.getAccountNumber() == null || response.getAccountNumber().isBlank()) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "findOrCreateAccount"));
        }
        Result<AccountId> accountIdResult = AccountId.valueOf(response.getAccountNumber());
        if (accountIdResult.isFailure()) {
            return Result.failure(accountIdResult.notification());
        }
        return AccountInfo.of(accountIdResult.orElseThrow(), loanTopic);
    }
}
