package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.AccountInfoResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FcbAccountMapper {

    public Result<AccountInfo> mapToAccountInfoFromOpenAccount(AccountInfoResponse response, LoanTopic loanTopic) {
        if (response.getAccountNumber() == null || response.getAccountNumber().isBlank()) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, "openAccount");
        }
        Result<AccountId> accountIdResult = AccountId.valueOf(response.getAccountNumber());
        if (accountIdResult.isFailure()) {
            return Result.failure(accountIdResult.err().orElseThrow());
        }
        return AccountInfo.of(accountIdResult.unwrap(), loanTopic);
    }

    public Result<AccountId> mapToAccountId(AccountInfoResponse response) {
        if (response.getAccountNumber() == null || response.getAccountNumber().isBlank()) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, "nova-open-account");
        }
        return AccountId.valueOf(response.getAccountNumber());
    }

    public Result<AccountNumber> mapToDeletedAccountNumber(AccountInfoResponse response) {
        if (response.getAccountNumber() == null || response.getAccountNumber().isBlank()) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, "deleteAccount");
        }
        return AccountNumber.of(response.getAccountNumber());
    }

    public Result<AccountNumber> mapToValidatedAccountNumber(AccountInfoResponse response) {
        if (response.getId() == null || response.getAccountNumber() == null) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, "validateAccountNumber");
        }
        return AccountNumber.of(response.getAccountNumber());
    }

    public Result<AccountInfo> mapToFindOrCreateAccountInfo(AccountInfoResponse response, LoanTopic loanTopic) {
        if (response.getAccountNumber() == null || response.getAccountNumber().isBlank()) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, "findOrCreateAccount");
        }
        Result<AccountId> accountIdResult = AccountId.valueOf(response.getAccountNumber());
        if (accountIdResult.isFailure()) {
            return Result.failure(accountIdResult.err().orElseThrow());
        }
        return AccountInfo.of(accountIdResult.unwrap(), loanTopic);
    }
}
