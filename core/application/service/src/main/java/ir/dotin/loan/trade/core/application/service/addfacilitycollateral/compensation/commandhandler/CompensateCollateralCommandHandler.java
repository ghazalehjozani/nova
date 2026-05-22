package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.error.FailureCause;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompensateCollateralCommandHandler implements CommandHandler<CompensateCollateralCommand> {

    private final TradeLoanFacilityRepository repository;
    private final CollateralServicePort collateralServicePort;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(CompensateCollateralCommand command) {
        return Result.fromOptional(
                        repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                        () -> FailureCause.businessRule(Notification.ofError(
                                TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, command.loanFacilityId())))
                .flatMap(facility -> revertCollaterals(facility, command));
    }

    private Result<List<DomainEvent<?>>> revertCollaterals(
            TradeLoanFacility facility, CompensateCollateralCommand command) {

        List<String> serialsToRevert = command.collateralSerials();

        if (serialsToRevert == null || serialsToRevert.isEmpty()) {
            log.warn(
                    "No collateral serials provided for compensation, facility: {}",
                    facility.getId().value());
            return Result.success(List.of());
        }

        if (facility.getLoanApplication().getApplicationNumber().isPresent()) {
            ApplicationNumber appNumber =
                    facility.getLoanApplication().getApplicationNumber().get();

            for (String serialValue : serialsToRevert) {
                CollateralSerial serial = CollateralSerial.of(serialValue).unwrap();

                collateralServicePort.unReserveCollateral(serial, appNumber, UUID.randomUUID(), command.uid());
            }
        }

        return facility.revertAddCollateral(serialsToRevert, clock)
                .map(v -> facility)
                .onSuccess(f -> {
                    repository.save(f);
                    log.info(
                            "Successfully reverted {} collaterals locally for facility: {}",
                            serialsToRevert.size(),
                            f.getId().value());
                })
                .onFailure(cause -> {
                    log.error(
                            "Failed to revert collaterals in domain for facility {}: {}",
                            facility.getId().value(),
                            cause.notification());
                })
                .map(TradeLoanFacility::domainEvents);
    }
}
