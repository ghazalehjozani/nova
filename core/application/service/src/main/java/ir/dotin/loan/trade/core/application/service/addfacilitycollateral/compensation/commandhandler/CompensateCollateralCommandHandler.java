package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.commandhandler;

import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.definition.WorkflowRoute;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.step.ReleasePreparation;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.step.RevertCollateralData;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.step.RevertCollateralStep;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralReservationReleaser;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class CompensateCollateralCommandHandler
        extends WorkflowCommandHandler<CompensateCollateralCommand, RevertCollateralData> {

    @Override
    protected Workflow<RevertCollateralData> route(WorkflowRoute<RevertCollateralData> route) {
        // @formatter:off
        return route.singleWrite("compensate-collateral", revertCollateralStep);
        // @formatter:on
    }

    @Override
    protected Result<RevertCollateralData> seed(CompensateCollateralCommand command) {
        return prepare(command).map(prepared -> new RevertCollateralData(command, prepared));
    }

    private Result<ReleasePreparation> prepare(CompensateCollateralCommand command) {
        List<String> serialsToRevert = command.collateralSerials();

        if (serialsToRevert == null || serialsToRevert.isEmpty()) {
            log.warn("No collateral serials provided for compensation, facility: {}", command.loanFacilityId());
            return Result.success(new ReleasePreparation(true));
        }

        Result<TradeLoanFacility> facilityResult = loadFacility(command);
        if (facilityResult.isFailure()) {
            return Result.failure(facilityResult.err().orElseThrow());
        }
        TradeLoanFacility facility = facilityResult.unwrap();

        if (facility.getLoanApplication().getApplicationNumber().isPresent()) {
            ApplicationNumber appNumber =
                    facility.getLoanApplication().getApplicationNumber().get();
            collateralReservationReleaser.release(appNumber, serialsToRevert, command.uid());
        }

        return Result.success(new ReleasePreparation(false));
    }

    private Result<TradeLoanFacility> loadFacility(CompensateCollateralCommand command) {
        return Result.fromOptional(
                repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())));
    }

    private final TradeLoanFacilityRepository repository;
    private final CollateralReservationReleaser collateralReservationReleaser;
    private final RevertCollateralStep revertCollateralStep;

    public CompensateCollateralCommandHandler(
            WorkflowEngine engine,
            TradeLoanFacilityRepository repository,
            CollateralReservationReleaser collateralReservationReleaser,
            RevertCollateralStep revertCollateralStep) {
        super(engine);
        this.repository = repository;
        this.collateralReservationReleaser = collateralReservationReleaser;
        this.revertCollateralStep = revertCollateralStep;
    }
}
