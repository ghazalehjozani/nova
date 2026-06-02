package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.time.Instant;
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

public record IssueFacilityContractSagaData(
        UUID facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        long expectedVersion,
        @Nullable String postedTransactionNumber,
        @Nullable String postedTrackingId,
        @Nullable Instant postedAt,
        @Nullable TransactionStatus transactionStatus,
        @Nullable Map<String, String> resolvedAccounts,
        @Nullable List<CapturedEventData> capturedEvents) {

    public static IssueFacilityContractSagaData initial(
            UUID facilityId, String branchCode, TransactionConfig transactionConfig, long expectedVersion) {
        return new IssueFacilityContractSagaData(
                facilityId, branchCode, transactionConfig, expectedVersion, null, null, null, null, null, List.of());
    }

    public IssueFacilityContractSagaData withResolvedAccounts(Map<String, String> accounts) {
        return new IssueFacilityContractSagaData(
                facilityId,
                branchCode,
                transactionConfig,
                expectedVersion,
                postedTransactionNumber,
                postedTrackingId,
                postedAt,
                transactionStatus,
                accounts,
                capturedEvents);
    }

    public IssueFacilityContractSagaData withPostedTransaction(
            String transactionNumber, String trackingId, TransactionStatus status, Instant posted) {
        return new IssueFacilityContractSagaData(
                facilityId,
                branchCode,
                transactionConfig,
                expectedVersion,
                transactionNumber,
                trackingId,
                posted,
                status,
                resolvedAccounts,
                capturedEvents);
    }

    public IssueFacilityContractSagaData withCapturedEvents(List<CapturedEventData> events) {
        return new IssueFacilityContractSagaData(
                facilityId,
                branchCode,
                transactionConfig,
                expectedVersion,
                postedTransactionNumber,
                postedTrackingId,
                postedAt,
                transactionStatus,
                resolvedAccounts,
                events);
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

    public Map<RelationType<?>, AccountId> getAccountIdsByRelationType() {
        return getResolvedAccounts().accountsByRelationType();
    }

    public record CapturedEventData(
            String eventType,
            UUID facilityId,
            @Nullable UUID sanctionedLoanId,
            @Nullable String transactionNumber,
            Instant occurredAt) {

        public static CapturedEventData contractIssued(
                String eventType,
                UUID facilityId,
                UUID sanctionedLoanId,
                String transactionNumber,
                Instant occurredAt) {
            return new CapturedEventData(eventType, facilityId, sanctionedLoanId, transactionNumber, occurredAt);
        }
    }
}
