package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SamatDto(
        @NotBlank @Pattern(regexp = "^\\d{16}$") String trackingNumber,
        String isicEconomicSector,
        String subIsicEconomicSector,
        String useType,
        String exceptionCode,
        String consumptionPlaceCode) {}
