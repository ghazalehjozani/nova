package ir.dotin.loan.trade.core.domain.installmentschedule.event;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public record GradualInstallmentsCreated(
        UUID eventId, InstallmentScheduleId aggregateId, Payload payload, Instant createdAt)
        implements InstallmentScheduleEvents<GradualInstallmentsCreated, GradualInstallmentsCreated.Payload> {

    public record Payload(
            UUID loanFacilityId, UUID installmentScheduleId, BigDecimal totalAmount, List<UUID> installmentIds) {
        public Payload {
            requireNonNull(loanFacilityId);
            requireNonNull(totalAmount);
            requireNonNull(installmentIds);
        }
    }

    public GradualInstallmentsCreated {
        requireNonNull(eventId);
        requireNonNull(aggregateId);
        requireNonNull(payload);
        requireNonNull(createdAt);
    }

    public static GradualInstallmentsCreated of(
            InstallmentScheduleId scheduleId,
            LoanFacilityId loanFacilityId,
            BigDecimal totalAmount,
            List<InstallmentId> specifications,
            Clock clock) {
        List<@NonNull UUID> installmentIds =
                specifications.stream().map(InstallmentId::value).toList();
        return new GradualInstallmentsCreated(
                randomUUID(),
                scheduleId,
                new Payload(loanFacilityId.value(), scheduleId.value(), totalAmount, installmentIds),
                clock.instant());
    }

    @Override
    public @NonNull String eventType() {
        return EVENT_TYPE_PREFIX + "GRADUAL_INSTALLMENTS_CREATED";
    }
}
