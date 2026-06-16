package ir.dotin.loan.trade.core.application.query.installmentschedule.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import ir.dotin.platform.pangaea.protocol.projection.api.ProjectableResource;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentStatus;

@ProjectableResource(
        views =
                @ProjectableResource.View(
                        name = "SUMMARY",
                        fields = {"id", "loanFacilityId", "status", "totalLoanAmount", "currency", "interestRate"}),
        expandable = {"installments.payments"})
public record TradeInstallmentScheduleQueryDto(
        UUID id,
        Long version,
        List<InstallmentEntityDto> installments,
        ScheduleHistoryEmbDto scheduleHistory,
        UUID loanFacilityId,
        BigDecimal totalLoanAmount,
        String currency,
        InstallmentScheduleType scheduleType,
        InstallmentScheduleStatus status,
        Instant initiatedAt,
        Instant lastModifiedAt,
        Integer gracePeriodDays,
        BigDecimal interestRate,
        RestructuringRecordDto restructuringRecord)
        implements Serializable, QueryResult {

    public record InstallmentEntityDto(
            UUID id,
            Long version,
            Integer sequenceNumber,
            BigDecimal totalAmount,
            String currency,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            LocalDate dueDate,
            InstallmentStatus status,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            LocalDate paidDate,
            List<InstallmentPaymentDto> payments)
            implements Serializable {}

    public record InstallmentPaymentDto(
            Integer installmentSequenceNumber,
            String paymentReference,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            BigDecimal totalPaidAmount,
            LocalDate valueDate,
            LocalDate paymentDate,
            String channel,
            String transactionReference,
            String remarks)
            implements Serializable {}

    public record ScheduleHistoryEmbDto(List<UUID> previousScheduleIds) implements Serializable {}

    public record RestructuringRecordDto(
            String reason,
            BigDecimal restructuringAmount,
            String restructuringAmountCurrency,
            Integer previousInstallmentCount,
            Integer newInstallmentCount,
            Integer unpaidInstallmentsCount,
            Integer preservedInstallmentsCount,
            Instant restructuredAt,
            String restructuredBy)
            implements Serializable {}
}
