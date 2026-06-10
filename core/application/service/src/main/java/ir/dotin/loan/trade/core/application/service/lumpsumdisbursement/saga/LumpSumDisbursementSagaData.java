package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.saga;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public record LumpSumDisbursementSagaData(
        UUID facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        LocalDate disbursementDate,
        long expectedVersion,
        @Nullable Map<String, String> resolvedAccounts,
        @Nullable List<PostedTransactionData> postedTransactions) {

    public static LumpSumDisbursementSagaData initial(
            UUID facilityId,
            String branchCode,
            TransactionConfig transactionConfig,
            LocalDate disbursementDate,
            long expectedVersion) {
        return new LumpSumDisbursementSagaData(
                facilityId, branchCode, transactionConfig, disbursementDate, expectedVersion, null, null);
    }

    public LumpSumDisbursementSagaData withResolvedAccounts(Map<String, String> accounts) {
        return new LumpSumDisbursementSagaData(
                facilityId,
                branchCode,
                transactionConfig,
                disbursementDate,
                expectedVersion,
                accounts,
                postedTransactions);
    }

    public LumpSumDisbursementSagaData withPostedTransactions(List<PostedTransactionData> transactions) {
        return new LumpSumDisbursementSagaData(
                facilityId,
                branchCode,
                transactionConfig,
                disbursementDate,
                expectedVersion,
                resolvedAccounts,
                transactions);
    }

    public ResolvedAccounts getResolvedAccounts() {
        if (resolvedAccounts == null || resolvedAccounts.isEmpty()) {
            return new ResolvedAccounts(Collections.emptyMap());
        }
        Map<RelationType<?>, AccountId> accounts = resolvedAccounts.entrySet().stream()
                .collect(
                        Collectors.toMap(e -> TradeRelationType.valueOf(e.getKey()), e -> new AccountId(e.getValue())));
        return new ResolvedAccounts(accounts);
    }

    public record PostedTransactionData(String transactionNumber, String trackingId, TransactionStatus status) {}
}
