package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;

import lombok.Builder;

@Builder(toBuilder = true)
public record UpdateCollateralCommand(UUID uid, Long version, UUID loanFacilityId, List<CollateralDto> collaterals)
        implements Command {

    public record CollateralDto(
            CollateralType collateralTypeCode, String description, String collateralSerial, MoneyDto usedAmount) {}

    public record MoneyDto(BigDecimal value, String currency) {}
}
