package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.commandhandler;

import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@Slf4j
@RequiredArgsConstructor
public final class CompensateCollateralCommandHandler
        implements WorkflowCommandHandler<CompensateCollateralCommand, RevertCollateralData> {

    @Override
    public Workflow<RevertCollateralData> definition() {
        // @formatter:off
        return Workflow.singleWrite("compensate-collateral", writePublishing(revertCollateralStep));
        // @formatter:on
    }

    @Override
    public Result<RevertCollateralData> seed(CompensateCollateralCommand command) {
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
}
