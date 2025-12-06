package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;

public record FullLoanFacilityLifecycleSagaData(
        OriginateLoanFacilityCommand originationCommand,
        String branchCode,
        TransactionConfig transactionConfig,
        DisbursementMethod disbursementMethod,
        UUID correlationId,
        @Nullable UUID facilityId,
        @Nullable UUID sanctionedLoanId,
        @Nullable UUID installmentScheduleId,
        @Nullable UUID previousInstallmentScheduleId,
        List<DomainEvent<?>> collectedDomainEvents) {

    public FullLoanFacilityLifecycleSagaData {
        collectedDomainEvents = collectedDomainEvents != null ? List.copyOf(collectedDomainEvents) : List.of();
    }

    public static FullLoanFacilityLifecycleSagaData initial(
            OriginateLoanFacilityCommand originationCommand,
            String branchCode,
            TransactionConfig transactionConfig,
            DisbursementMethod disbursementMethod,
            UUID correlationId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                branchCode,
                transactionConfig,
                disbursementMethod,
                correlationId,
                null,
                null,
                null,
                null,
                List.of());
    }

    public FullLoanFacilityLifecycleSagaData withFacilityId(UUID facilityId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                branchCode,
                transactionConfig,
                disbursementMethod,
                correlationId,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData withSanctionedLoanId(UUID sanctionedLoanId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                branchCode,
                transactionConfig,
                disbursementMethod,
                correlationId,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData withInstallmentScheduleId(UUID installmentScheduleId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                branchCode,
                transactionConfig,
                disbursementMethod,
                correlationId,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData withPreviousInstallmentScheduleId(UUID previousId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                branchCode,
                transactionConfig,
                disbursementMethod,
                correlationId,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousId,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData addDomainEvents(List<DomainEvent<?>> events) {
        if (events == null || events.isEmpty()) return this;
        var combined = new ArrayList<>(this.collectedDomainEvents);
        combined.addAll(events);
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                branchCode,
                transactionConfig,
                disbursementMethod,
                correlationId,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                Collections.unmodifiableList(combined));
    }
}
