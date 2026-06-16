package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.messaging.api.command.CommandPayload;

public record InstallmentPaymentMessage(
        String operationType,
        String fileNumber,
        String transactionNumber,
        @Nullable String documentNumber,
        String currency,
        BigDecimal totalPrincipalAmount,
        BigDecimal totalInterestAmount,
        List<PaymentDetailDto> payments,
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
