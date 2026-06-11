package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow;

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

public record LumpSumData(
        UUID facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        LocalDate disbursementDate,
        long expectedVersion,
        @Nullable Map<String, String> resolvedAccounts,
        @Nullable List<PostedTransactionData> postedTransactions) {

    public static LumpSumData initial(
            UUID facilityId,
            String branchCode,
            TransactionConfig transactionConfig,
            LocalDate disbursementDate,
            long expectedVersion) {
        return new LumpSumData(
                facilityId, branchCode, transactionConfig, disbursementDate, expectedVersion, null, null);
    }

    public LumpSumData withResolvedAccounts(Map<String, String> accounts) {
        return new LumpSumData(
                facilityId,
                branchCode,
                transactionConfig,
                disbursementDate,
                expectedVersion,
                accounts,
                postedTransactions);
    }

    public LumpSumData withPostedTransactions(List<PostedTransactionData> transactions) {
        return new LumpSumData(
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
