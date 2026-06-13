package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateFacilityStateStep implements PublishingWriteActivity<ContractData>, Compensable<ContractData> {

    private final TradeLoanFacilityRepository facilityRepository;
    private final Clock clock;

    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();

        var result = loadFacility(LoanFacilityId.of(data.facilityId())).flatMap(facility -> {
            var trackedNumber = TrackedTransactionNumber.create(
                    Objects.requireNonNull(data.postedTransactionNumber(), "postedTransactionNumber"),
                    Objects.requireNonNull(data.postedTrackingId(), "postedTrackingId"),
                    Objects.requireNonNull(data.transactionStatus(), "transactionStatus"),
                    clock);

            return facility.issueContract(trackedNumber, data.getAccountIdsByRelationType(), clock)
                    .map(ignored -> {
                        List<DomainEvent<?>> events = List.copyOf(facility.domainEvents());
                        facilityRepository.save(facility, data.expectedVersion());
                        log.info("Contract issued: facilityId={}", data.facilityId());
                        return events;
                    });
        });

        return StepResult.fromWriteResult(result);
    }

    public StepResult<Void> compensate(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }

        var facility = facilityResult.unwrap();
        var revertResult = facility.revertContractIssuance(clock);

        if (revertResult.isFailure()) {
            return StepResult.fromResult(revertResult);
        }

        facilityRepository.save(facility);

        log.warn("Reverted contract issuance: facilityId={}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }
}
