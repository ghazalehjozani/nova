package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Builder;

@Builder
public record CurrencyTypeDto(
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String value) {}
