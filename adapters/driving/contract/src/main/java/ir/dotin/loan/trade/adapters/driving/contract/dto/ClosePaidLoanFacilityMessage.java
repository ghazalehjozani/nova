package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.messaging.api.command.CommandPayload;

public record ClosePaidLoanFacilityMessage(
        String producerCode,
        String eventUid,
        @Nullable Date dateTime,
        int version,
        @Nullable String responseTopic,
        @Nullable String[] tags,
        String operationType,
        String fileNumber,
        String transactionNumber,
        @Nullable String documentNumber,
        String currency,
        String settleType,
        boolean badDebtsSettlement,
        String deathDate,
        BigDecimal totalPrincipalAmount,
        BigDecimal totalInterestAmount,
        BigDecimal totalPenaltyAmount,
        List<PaymentDetailDto> payments,
        String settleReasonType,
        @Nullable String channel,
        Map<String, Object> metadata)
        implements CommandPayload {

    public record PaymentDetailDto(
            int installmentSequenceNumber,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            @Nullable BigDecimal penaltyAmount,
            BigDecimal totalAmount,
            @Nullable String valueDate,
            @Nullable String paymentDate,
            boolean fullySettled) {}
}
