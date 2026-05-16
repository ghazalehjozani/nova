package ir.dotin.loan.trade.core.application.service.updatefacilitycollateral.commandhandler;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
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
                .peekValue(tradeLoanFacilityRepository::save)
                .peekValue(_ -> log.debug(
                        "update  collateral completed: applicationNumber={}, collateral={} ",
                        command.applicationNumber(),
                        command.collaterals().size()))
                .map(AbstractAggregateRoot::domainEvents);
    }

    private Result<TradeLoanFacility> loadFacility(UpdateCollateralCommand command) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findByApplicationNumber(command.applicationNumber()),
                Notification.ofError(
                        TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING, command.applicationNumber()));
    }

    private List<Collateral> buildCollateral(
            List<UpdateCollateralCommand.CollateralItem> collateralItems, CurrencyType currencyType) {
        if (collateralItems == null) {
            return null;
        }

        List<Collateral> list = new ArrayList<Collateral>(collateralItems.size());
        for (UpdateCollateralCommand.CollateralItem item : collateralItems) {
            list.add(mapper.toCollateral(item, currencyType));
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
