package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.step;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.RemoteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralReservationReleaser;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.CollateralData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReserveCollateralsStep implements RemoteActivity<CollateralData>, Compensable<CollateralData> {

    private static final Integer RESERVE_DURATION_MINUTES = 1440;

    private final AddFacilityCollateralCommandMapper mapper;
    private final TradeLoanFacilityRepository facilityRepository;
    private final CollateralServicePort collateralServicePort;
    private final CollateralReservationReleaser collateralReservationReleaser;

    public StepResult<Void> execute(WorkflowContext<CollateralData> ctx) {
        var data = ctx.data();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.failure(facilityResult.err().orElseThrow());
        }
        var facility = facilityResult.unwrap();

        if (facility.getLoanApplication().getApplicationNumber().isEmpty()) {
            return StepResult.failure(FailureCause.businessRule(
                    Notification.ofError(TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING)));
        }
        ApplicationNumber appNumber =
                facility.getLoanApplication().getApplicationNumber().get();

        List<Collateral> collaterals = mapper.toCollaterals(data.collaterals());
        List<String> reserved = new ArrayList<>();

        for (Collateral collateral : collaterals) {
            Result<List<CollateralSerial>> result = collateralServicePort.reserveCollateral(
                    collateral.collateralSerial(),
                    appNumber,
                    data.requestId(),
                    RESERVE_DURATION_MINUTES,
                    collateral.usedAmount());

            if (result.isFailure()) {
                collateralReservationReleaser.release(appNumber, reserved, data.requestId());
                return StepResult.failure(result.err().orElseThrow());
            }
            reserved.add(collateral.collateralSerial().value());
        }

        ctx.updateData(d -> d.withReservedSerials(reserved));
        log.info("Reserved {} collaterals for facility: {}", reserved.size(), data.facilityId());
        return new StepResult.Success<>(null);
    }

    public StepResult<Void> compensate(WorkflowContext<CollateralData> ctx) {
        var data = ctx.data();
        List<String> reservedSerials = data.reservedSerials() == null ? List.of() : data.reservedSerials();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }
        var facility = facilityResult.unwrap();

        if (facility.getLoanApplication().getApplicationNumber().isEmpty()) {
            log.warn("No application number for facility {}; skipping un-reserve", data.facilityId());
            return new StepResult.Success<>(null);
        }
        ApplicationNumber appNumber =
                facility.getLoanApplication().getApplicationNumber().get();

        collateralReservationReleaser.release(appNumber, reservedSerials, data.requestId());
        log.warn("Un-reserved {} collaterals for facility: {}", reservedSerials.size(), data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }
}
