package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.messaging.api.command.CommandPayload;

public record ClosePaidLoanFacilityMessage(
        String operationType,
        String fileNumber,
        String transactionNumber,
        @Nullable String documentNumber,
        String currency,
        String settleType,
        boolean badDebtsSettlement,
        LocalDate deathDate,
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
            @Nullable LocalDate valueDate,
            @Nullable LocalDate paymentDate,
            boolean fullySettled) {}
}
