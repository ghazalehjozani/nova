package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.WriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.AbstractCollateralValidationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.CollateralData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddCollateralStep implements WriteActivity<CollateralData>, Compensable<CollateralData> {

    private final AddFacilityCollateralCommandMapper mapper;
    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository arrangementRepository;
    private final TradeLoanFacilityService domainService;
    private final AbstractCollateralValidationService collateralValidationService;
    private final Clock clock;

    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<CollateralData> ctx) {
        var data = ctx.data();

        var result = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadArrangement(facility).flatMap(arrangement -> {
                    List<Collateral> collaterals = mapper.toCollaterals(data.collaterals());
                    return domainService
                            .addCollateral(facility, collaterals)
                            .flatMap(ignored ->
                                    collateralValidationService.validateFacilityCollaterals(facility, arrangement))
                            .map(ignored -> {
                                List<DomainEvent<?>> events = List.copyOf(facility.domainEvents());
                                facilityRepository.save(facility, data.expectedVersion());
                                log.info(
                                        "{} collaterals added for facility: {}", collaterals.size(), data.facilityId());
                                return events;
                            });
                }));

        return StepResult.fromWriteResult(result);
    }

    public StepResult<Void> compensate(WorkflowContext<CollateralData> ctx) {
        var data = ctx.data();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }
        var facility = facilityResult.unwrap();

        List<String> serials = data.collaterals().stream()
                .map(AddFacilityCollateralCommand.CollateralDto::collateralSerial)
                .toList();

        var revertResult = facility.revertAddCollateral(serials, clock);
        if (revertResult.isFailure()) {
            return StepResult.fromResult(revertResult);
        }

        facilityRepository.save(facility);
        log.warn("Reverted add-collateral for facility: {}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<TradeLoanArrangement> loadArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                arrangementRepository.findById(facility.getLoanArrangementId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                        facility.getLoanArrangementId(),
                        facility.getId().value())));
    }
}
