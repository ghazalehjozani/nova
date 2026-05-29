package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCollateralCommandHandler implements CommandHandler<UpdateCollateralCommand> {

    private static final Logger log = LoggerFactory.getLogger(UpdateCollateralCommandHandler.class);

    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final Clock clock;
    private final AddFacilityCollateralCommandMapper mapper;

    @Override
    public Result<List<DomainEvent<?>>> handle(UpdateCollateralCommand command) {
        return loadFacility(command)
                .flatMap(tradeLoanFacility -> updateCollateral(tradeLoanFacility, command))
                .onSuccess(tradeLoanFacilityRepository::save)
                .onSuccess(_ -> log.debug(
                        "update  collateral completed: applicationNumber={}, collateral={} ",
                        command.applicationNumber(),
                        command.collaterals().size()))
                .map(AbstractAggregateRoot::domainEvents);
    }

    private Result<TradeLoanFacility> loadFacility(UpdateCollateralCommand command) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findByApplicationNumber(command.applicationNumber()),
                FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber())));
    }

    private List<Collateral> buildCollateral(
            List<UpdateCollateralCommand.CollateralItem> collateralItems, CurrencyType currencyType) {
        if (collateralItems == null) {
            return Collections.emptyList();
        }

        List<Collateral> list = new ArrayList<Collateral>(collateralItems.size());
        for (UpdateCollateralCommand.CollateralItem item : collateralItems) {
            var collateral = mapper.toCollateral(item, currencyType);
            if (collateral != null) {
                list.add(collateral);
            }
        }

        return list;
    }

    private Result<TradeLoanFacility> updateCollateral(
            TradeLoanFacility tradeLoanFacility, UpdateCollateralCommand command) {
        tradeLoanFacility.updateCollateral(
                buildCollateral(
                        command.collaterals(),
                        tradeLoanFacility.getLoanApplication().getCurrency()),
                clock);
        return Result.success(tradeLoanFacility);
    }
}
