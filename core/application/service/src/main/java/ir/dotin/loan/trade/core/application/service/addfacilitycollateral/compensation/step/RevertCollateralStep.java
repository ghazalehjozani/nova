package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.WriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevertCollateralStep implements WriteActivity<RevertCollateralData> {

    private final TradeLoanFacilityRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<RevertCollateralData> ctx) {
        RevertCollateralData data = ctx.data();
        return StepResult.fromWriteResult(write(data.command(), data.prepared()));
    }

    private Result<List<DomainEvent<?>>> write(CompensateCollateralCommand command, ReleasePreparation prepared) {
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
}
