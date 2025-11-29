package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.TransactionStatus;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

public record IssueFacilityContractSagaData(
        UUID facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        @Nullable String postedTransactionNumber,
        @Nullable String postedTrackingId,
        @Nullable Instant postedAt,
        @Nullable TransactionStatus transactionStatus,
        @Nullable Map<String, String> accountIds,
        @Nullable List<CapturedEventData> capturedEvents) {

    public static IssueFacilityContractSagaData initial(
            UUID facilityId, String branchCode, TransactionConfig transactionConfig) {
        return new IssueFacilityContractSagaData(
                facilityId, branchCode, transactionConfig, null, null, null, null, null, List.of());
    }

    public IssueFacilityContractSagaData withAccountIds(Map<RelationType<?>, AccountId> accountIds) {
        Map<String, String> stringKeyMap = accountIds.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> ((Enum<?>) e.getKey()).name(), e -> e.getValue().value()));

        return new IssueFacilityContractSagaData(
                facilityId,
                branchCode,
                transactionConfig,
                postedTransactionNumber,
                postedTrackingId,
                postedAt,
                transactionStatus,
                stringKeyMap,
                capturedEvents);
    }

    public IssueFacilityContractSagaData withPostedTransaction(
            String transactionNumber, String trackingId, TransactionStatus transactionStatus, Instant postedAt) {
        return new IssueFacilityContractSagaData(
                facilityId,
                branchCode,
                transactionConfig,
                transactionNumber,
                trackingId,
                postedAt,
                transactionStatus,
                accountIds,
                capturedEvents);
    }

    public IssueFacilityContractSagaData withCapturedEvents(List<CapturedEventData> events) {
        return new IssueFacilityContractSagaData(
                facilityId,
                branchCode,
                transactionConfig,
                postedTransactionNumber,
                postedTrackingId,
                postedAt,
                transactionStatus,
                accountIds,
                events);
    }

    public Map<RelationType<?>, AccountId> getAccountIdsByRelationType() {
        if (accountIds == null) return Collections.emptyMap();

        return accountIds.entrySet().stream()
                .collect(Collectors.toMap(e -> resolveRelationType(e.getKey()), e -> AccountId.valueOf(e.getValue())
                        .getValue()));
    }

    private RelationType<?> resolveRelationType(String key) {
        try {
            return TradeRelationType.valueOf(key);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Unknown RelationType in Saga Data: " + key, e);
        }
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
