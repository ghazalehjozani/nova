package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.dispatcher.api.command.Command;

public record CloseFacilityPaidOffCommand(
        UUID uid,
        Long version,
        String applicationNumber,
        String transactionReference,
        String currency,
        List<InstallmentPaymentItem> payments,
        @Nullable String channel,
        @Nullable String legacyTransactionReference,
        @Nullable TransactionMetadata transactionMetadata)
        implements Command {

    public record InstallmentPaymentItem(
            int installmentSequenceNumber,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            BigDecimal totalPaidAmount,
            LocalDate valueDate,
            LocalDate paymentDate,
            boolean fullySettled) {}

    public record TransactionMetadata(
            @Nullable String correlationId, @Nullable String initiatedBy) {}
}
