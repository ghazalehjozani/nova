package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;

@Builder(toBuilder = true)
public record EconomicSectorDto(@NotBlank String code) {}
