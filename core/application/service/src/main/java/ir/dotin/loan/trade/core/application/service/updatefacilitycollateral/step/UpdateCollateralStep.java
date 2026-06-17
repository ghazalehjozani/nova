package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.step;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateCollateralStep implements PublishingWriteActivity<UpdateCollateralData> {

    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final Clock clock;
    private final AddFacilityCollateralCommandMapper mapper;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<UpdateCollateralData> ctx) {
        return StepResult.fromWriteResult(write(ctx.data().command()));
    }

    private Result<List<DomainEvent<?>>> write(UpdateFacilityCollateralCommand command) {
        return loadFacility(command)
                .flatMap(tradeLoanFacility -> updateCollateral(tradeLoanFacility, command))
                .onSuccess(tradeLoanFacility -> tradeLoanFacilityRepository.save(tradeLoanFacility, command.version()))
                .onSuccess(_ -> log.debug(
                        "update  collateral completed: applicationNumber={}, collateral={} ",
                        command.applicationNumber(),
                        command.collaterals().size()))
                .map(AbstractAggregateRoot::domainEvents);
    }

    private Result<TradeLoanFacility> loadFacility(UpdateFacilityCollateralCommand command) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findByApplicationNumber(command.applicationNumber()),
                FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private List<Collateral> buildCollateral(
            List<UpdateFacilityCollateralCommand.CollateralItem> collateralItems, CurrencyType currencyType) {
        if (collateralItems == null) {
            return new ArrayList<>();
        }

        List<Collateral> list = new ArrayList<>(collateralItems.size());
        for (UpdateFacilityCollateralCommand.CollateralItem item : collateralItems) {
            var collateral = mapper.toCollateral(item, currencyType);
            if (collateral != null) {
                list.add(collateral);
            }
        }

        return list;
    }

    private Result<TradeLoanFacility> updateCollateral(
            TradeLoanFacility tradeLoanFacility, UpdateFacilityCollateralCommand command) {
        tradeLoanFacility.updateCollateral(
                buildCollateral(
                        command.collaterals(),
                        tradeLoanFacility.getLoanApplication().getCurrency()),
                clock);
        return Result.success(tradeLoanFacility);
    }
}
