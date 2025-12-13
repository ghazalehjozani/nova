package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;

public record FullLoanFacilityLifecycleSagaData(
        OriginateLoanFacilityCommand originationCommand,
        List<FullLoanFacilityLifecycleCommand.CollateralDto> collaterals,
        BigDecimal trancheAmount,
        String branchCode,
        TransactionConfig transactionConfig,
        DisbursementMethod disbursementMethod,
        LocalDate disbursementDate,
        UUID correlationId,
        String confirmType,
        @Nullable UUID facilityId,
        @Nullable UUID sanctionedLoanId,
        @Nullable UUID installmentScheduleId,
        @Nullable UUID previousInstallmentScheduleId,
        @Nullable List<String> addedCollateralSerials,
        List<DomainEvent<?>> collectedDomainEvents) {

    public FullLoanFacilityLifecycleSagaData {
        collectedDomainEvents = collectedDomainEvents != null ? List.copyOf(collectedDomainEvents) : List.of();
    }

    public static FullLoanFacilityLifecycleSagaData initial(
            OriginateLoanFacilityCommand originationCommand,
            List<FullLoanFacilityLifecycleCommand.CollateralDto> collaterals,
            BigDecimal trancheAmount,
            String branchCode,
            TransactionConfig transactionConfig,
            DisbursementMethod disbursementMethod,
            LocalDate disbursementDate,
            UUID correlationId,
            String confirmType) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType,
                null,
                null,
                null,
                null,
                null,
                List.of());
    }

    public FullLoanFacilityLifecycleSagaData withFacilityId(UUID facilityId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                addedCollateralSerials,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData withSanctionedLoanId(UUID sanctionedLoanId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                addedCollateralSerials,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData withInstallmentScheduleId(UUID installmentScheduleId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                addedCollateralSerials,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData withPreviousInstallmentScheduleId(UUID previousId) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                addedCollateralSerials,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData withAddedCollateralSerials(List<String> serials) {
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                serials,
                collectedDomainEvents);
    }

    public FullLoanFacilityLifecycleSagaData addDomainEvents(List<DomainEvent<?>> events) {
        if (events == null || events.isEmpty()) return this;
        var combined = new ArrayList<>(this.collectedDomainEvents);
        combined.addAll(events);
        return new FullLoanFacilityLifecycleSagaData(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType,
                facilityId,
                sanctionedLoanId,
                installmentScheduleId,
                previousInstallmentScheduleId,
                addedCollateralSerials,
                Collections.unmodifiableList(combined));
    }

    public boolean hasCollaterals() {
        return collaterals != null && !collaterals.isEmpty();
    }

    public boolean hasAddedCollaterals() {
        return addedCollateralSerials != null && !addedCollateralSerials.isEmpty();
    }

    public int getAddedCollateralCount() {
        return addedCollateralSerials != null ? addedCollateralSerials.size() : 0;
    }
}
