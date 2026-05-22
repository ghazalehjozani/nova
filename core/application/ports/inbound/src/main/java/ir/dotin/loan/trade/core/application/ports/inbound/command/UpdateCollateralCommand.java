package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;

public record UpdateCollateralCommand(
        UUID uid, Long version, String applicationNumber, List<CollateralItem> collaterals) implements Command {

    public record CollateralItem(String collateralSerial, BigDecimal usedAmount) {}
}
