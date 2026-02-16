package ir.dotin.loan.trade.adapters.driving.messaging.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.adapter.messaging.command.model.CommandPayload;

/**
 * Inbound message consumed from Kafka topic: {@code corridor.core.loan.nova.installment-operation.request.queue.v1}
 * with operationType = "INSTALLMENT_COLLECTION".
 *
 * <p>Wire format matches {@code InstallmentCollectionMessage} published by the old system (Java 8). No
 * {@code @JsonTypeInfo} — type discrimination is handled by the consumer via the {@code operationType} field.
 *
 * <p><b>Note:</b> The old system sends {@code fileNumber} (loan file number), not UUIDs. The command mapper must
 * resolve {@code loanFacilityId} and {@code installmentScheduleId} from the {@code fileNumber} via repository lookup.
 */
public record InstallmentPaymentMessage(
        // --- Corridor envelope (from AbstractMessage) ---
        String producerCode,
        String eventUid,
        @Nullable Date dateTime,
        int version,
        @Nullable String responseTopic,
        @Nullable String[] tags,

        // --- Nova envelope (from NovaInstallmentEnvelope) ---
        String operationType,
        String fileNumber,

        // --- Transaction identity ---
        String transactionNumber,
        @Nullable String documentNumber,
        @Nullable String depositNumber,
        String currency,

        // --- Summary totals ---
        BigDecimal totalPrincipalAmount,
        BigDecimal totalInterestAmount,
        @Nullable BigDecimal totalPenaltyAmount,
        @Nullable BigDecimal totalDeferredInterestAmount,
        @Nullable BigDecimal totalPenaltyWaiverAmount,
        @Nullable BigDecimal totalLifeInsuranceAmount,
        @Nullable BigDecimal totalLifeInsurancePenalty,
        @Nullable BigDecimal totalIncomeAmount,

        // --- Operational context ---
        @Nullable String channel,
        @Nullable String effectiveDateDescription,
        boolean commitmentInterestOperation,
        boolean dailyInterest,

        // --- Per-installment details ---
        List<PaymentDetailDto> payments,
        Map<String, Object> metadata)
        implements CommandPayload {

    public record PaymentDetailDto(
            int installmentSequenceNumber,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            @Nullable BigDecimal penaltyAmount,
            @Nullable BigDecimal deferredInterestAmount,
            @Nullable BigDecimal penaltyWaiverAmount,
            @Nullable BigDecimal lifeInsuranceAmount,
            @Nullable BigDecimal lifeInsurancePenalty,
            BigDecimal totalAmount,
            @Nullable String valueDate,
            @Nullable String paymentDate,
            boolean fullySettled) {}
}
