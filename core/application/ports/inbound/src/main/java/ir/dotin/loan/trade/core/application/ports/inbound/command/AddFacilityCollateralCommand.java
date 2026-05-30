package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;

import lombok.Builder;

@Builder(toBuilder = true)
public record AddFacilityCollateralCommand(
        @NotNull UUID uid,
        @NotNull Long version,
        @NotNull UUID loanFacilityId,
        @NotEmpty List<CollateralDto> collaterals)
        implements Command {

    @Builder
    public record CollateralDto(
            @NotBlank CollateralType collateralTypeCode,
            @NotBlank String description,
            @NotBlank String collateralSerial,
            @NotNull @Valid MoneyDto usedAmount) {}

    public record MoneyDto(
            @NotNull BigDecimal value, @NotBlank String currency) {}
}
