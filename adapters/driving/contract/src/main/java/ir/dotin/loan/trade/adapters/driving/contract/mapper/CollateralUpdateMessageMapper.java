package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.CollateralUpdateMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.CollateralUpdateMessage.CollateralDetailDto;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateFacilityCollateralCommand.CollateralItem;

@Component
public class CollateralUpdateMessageMapper {
    public UpdateFacilityCollateralCommand toCommand(CollateralUpdateMessage message) {
        List<CollateralItem> collaterals = message.collaterals() != null
                ? message.collaterals().stream().map(this::toCollateralItem).toList()
                : List.of();

        // version: no optimistic-lock check for message-driven collateral update; 0L = unversioned sentinel.
        return new UpdateFacilityCollateralCommand(UUID.randomUUID(), 0L, message.fileNumber(), collaterals);
    }

    private CollateralItem toCollateralItem(CollateralDetailDto dto) {
        return new CollateralItem(dto.collateralSerial(), dto.usedAmount());
    }
}
