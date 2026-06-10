package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.commandhandler;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteCommandHandler;
import ir.dotin.platform.pangaea.servicelayer.transaction.WriteTransaction;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralReservationReleaser;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CompensateCollateralCommandHandler
        extends WriteCommandHandler<
                CompensateCollateralCommand, CompensateCollateralCommandHandler.ReleasePreparation> {

    private final TradeLoanFacilityRepository repository;
    private final CollateralReservationReleaser collateralReservationReleaser;
    private final Clock clock;

    public CompensateCollateralCommandHandler(
            WriteTransaction writeTransaction,
            TradeLoanFacilityRepository repository,
            CollateralReservationReleaser collateralReservationReleaser,
            Clock clock) {
        super(writeTransaction);
        this.repository = repository;
        this.collateralReservationReleaser = collateralReservationReleaser;
        this.clock = clock;
    }

    @Override
    protected Result<ReleasePreparation> prepare(CompensateCollateralCommand command) {
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

    @Override
    protected Result<List<DomainEvent<?>>> write(CompensateCollateralCommand command, ReleasePreparation prepared) {
        if (prepared.noOp()) {
            return Result.success(List.of());
        }
        return revertCollaterals(command, command.collateralSerials());
    }

    private Result<List<DomainEvent<?>>> revertCollaterals(
            CompensateCollateralCommand command, List<String> serialsToRevert) {

        return loadFacility(command)
                .flatMap(facility ->
                        facility.revertAddCollateral(serialsToRevert, clock).map(v -> facility))
                .onSuccess(f -> {
                    repository.save(f);
                    log.info(
                            "Successfully reverted {} collaterals locally for facility: {}",
                            serialsToRevert.size(),
                            f.getId().value());
                })
                .onFailure(cause -> log.error(
                        "Failed to revert collaterals in domain for facility {}: {}",
                        command.loanFacilityId(),
                        cause.notification()))
                .map(TradeLoanFacility::domainEvents);
    }

    private Result<TradeLoanFacility> loadFacility(CompensateCollateralCommand command) {
        return Result.fromOptional(
                repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())));
    }

    record ReleasePreparation(boolean noOp) {}
}
