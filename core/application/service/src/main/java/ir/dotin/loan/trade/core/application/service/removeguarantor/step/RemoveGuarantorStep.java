package ir.dotin.loan.trade.core.application.service.removeguarantor.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.removeguarantor.commandhandler.RemoveGuarantorCommandHandler;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveGuarantorStep implements PublishingWriteActivity<RemoveGuarantorCommandHandler.Data> {

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository arrangementRepository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<RemoveGuarantorCommandHandler.Data> ctx) {
        var data = ctx.data();

        var result = loadFacility(LoanFacilityId.of(data.facilityId())).flatMap(facility -> {
            int requiredMin = resolveRequiredMinGuarantors(facility);
            return facility.removeGuarantor(data.customerNumber(), requiredMin, clock)
                    .map(ignored -> {
                        List<DomainEvent<?>> events = List.copyOf(facility.domainEvents());
                        facilityRepository.save(facility, data.expectedVersion());
                        log.info("Removed guarantor {} for facility: {}", data.customerNumber(), data.facilityId());
                        return events;
                    });
        });

        return StepResult.fromWriteResult(result);
    }

    private int resolveRequiredMinGuarantors(TradeLoanFacility facility) {
        return arrangementRepository
                .findById(facility.getLoanArrangementId())
                .map(TradeLoanArrangement::getGuarantorCount)
                .orElse(0);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }
}
