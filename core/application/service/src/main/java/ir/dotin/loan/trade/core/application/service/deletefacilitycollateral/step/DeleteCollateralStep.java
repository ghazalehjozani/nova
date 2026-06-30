package ir.dotin.loan.trade.core.application.service.deletefacilitycollateral.step;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.deletefacilitycollateral.workflow.DeleteCollateralData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteCollateralStep implements PublishingWriteActivity<DeleteCollateralData> {

    private final TradeLoanFacilityRepository facilityRepository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<DeleteCollateralData> ctx) {
        DeleteCollateralData data = ctx.data();

        Result<List<DomainEvent<?>>> result = loadFacility(data.facilityId())
                .flatMap(facility -> facility.deleteCollaterals(data.collateralSerials(), clock)
                        .map(ignored -> {
                            facilityRepository.save(facility, data.expectedVersion());
                            log.info(
                                    "Deleted {} collaterals from facility {}",
                                    data.collateralSerials().size(),
                                    data.facilityId());
                            return facility.domainEvents();
                        }));

        return StepResult.fromWriteResult(result);
    }

    private Result<TradeLoanFacility> loadFacility(UUID facilityId) {
        return Result.fromOptional(
                facilityRepository.findById(LoanFacilityId.of(facilityId)),
                () -> FailureCause.notFound(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, facilityId)));
    }
}
