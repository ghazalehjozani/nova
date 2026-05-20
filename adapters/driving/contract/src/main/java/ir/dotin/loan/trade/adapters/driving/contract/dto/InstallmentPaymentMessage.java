package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.messaging.api.command.CommandPayload;
import ir.dotin.platform.messaging.api.version.ContractVersion;

@ContractVersion(1)
public record InstallmentPaymentMessage(
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
            @Nullable String valueDate,
            @Nullable String paymentDate,
            boolean fullySettled) {}
}
