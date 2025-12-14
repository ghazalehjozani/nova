package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SamatDto(
        @NotBlank @Valid @Pattern(regexp = "^\\d{16}$") String trackingNumber,
        @Nullable String isicEconomicSector,
        @Nullable String subIsicEconomicSector,
        @Nullable String useType,
        @Nullable String exceptionCode,
        @Nullable String consumptionPlaceCode) {}
