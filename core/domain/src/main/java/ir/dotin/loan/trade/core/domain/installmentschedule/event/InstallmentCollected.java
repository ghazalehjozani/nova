package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentPaymentRecord;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record InstallmentCollected(
        UUID eventId,
        UUID aggregateId,
        String eventType,
        UUID loanFacilityId,
        String paymentReference,
        int installmentSequenceNumber,
        BigDecimal principalAmount,
        BigDecimal interestAmount,
        BigDecimal totalPaidAmount,
        LocalDate valueDate,
        LocalDate paymentDate,
        @Nullable String channel,
        @Nullable String legacyTransactionReference,
        String installmentStatus,
        Instant createdAt)
        implements InstallmentScheduleEvents<InstallmentCollected> {

    public InstallmentCollected {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(eventType);
        requireNonNull(loanFacilityId);
        requireNonNull(paymentReference);
        requireNonNull(principalAmount);
        requireNonNull(interestAmount);
        requireNonNull(totalPaidAmount);
        requireNonNull(valueDate);
        requireNonNull(paymentDate);
        requireNonNull(installmentStatus);
        requireNonNull(createdAt);
    }

    public static InstallmentCollected of(
            @NonNull InstallmentScheduleId scheduleId,
            @NonNull LoanFacilityId loanFacilityId,
            @NonNull InstallmentPaymentRecord paymentRecord,
            @NonNull InstallmentStatus installmentStatus,
            Instant occurredAt) {

        return new InstallmentCollected(
                randomUUID(),
                scheduleId.value(),
                InstallmentScheduleEventType.INSTALLMENT_COLLECTED.getFullType(),
                loanFacilityId.value(),
                paymentRecord.paymentReference(),
                paymentRecord.installmentSequenceNumber(),
                paymentRecord.principalAmount().value(),
                paymentRecord.interestAmount().value(),
                paymentRecord.totalPaidAmount().value(),
                paymentRecord.valueDate(),
                paymentRecord.paymentDate(),
                paymentRecord.channel(),
                paymentRecord.legacyTransactionReference(),
                installmentStatus.name(),
                occurredAt);
    }
}
