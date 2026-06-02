package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.saga;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;

/**
 * Mutable saga state for add-facility-collateral, threaded through the steps via
 * {@code SagaContext.updateSagaData(...)}. Implemented as an immutable record with {@code withXxx} withers, mirroring
 * {@code IssueFacilityContractSagaData}.
 */
public record AddFacilityCollateralSagaData(
        UUID facilityId,
        UUID requestId,
        List<AddFacilityCollateralCommand.CollateralDto> collaterals,
        long expectedVersion,
        @Nullable List<String> reservedSerials,
        @Nullable List<CapturedEventData> capturedEvents) {

    public static AddFacilityCollateralSagaData initial(
            UUID facilityId,
            UUID requestId,
            List<AddFacilityCollateralCommand.CollateralDto> collaterals,
            long expectedVersion) {
        return new AddFacilityCollateralSagaData(
                facilityId, requestId, collaterals, expectedVersion, List.of(), List.of());
    }

    public AddFacilityCollateralSagaData withReservedSerials(List<String> serials) {
        return new AddFacilityCollateralSagaData(
                facilityId, requestId, collaterals, expectedVersion, serials, capturedEvents);
    }

    public AddFacilityCollateralSagaData withCapturedEvents(List<CapturedEventData> events) {
        return new AddFacilityCollateralSagaData(
                facilityId, requestId, collaterals, expectedVersion, reservedSerials, events);
    }

    /** Flat, serializable snapshot of a {@code TradeLoanFacilityCollateralAdded} event for re-surfacing post-saga. */
    public record CapturedEventData(
            UUID eventId,
            UUID aggregateId,
            String eventType,
            UUID sanctionedLoanId,
            List<String> collateralSerials,
            Instant createdAt) {}
}
