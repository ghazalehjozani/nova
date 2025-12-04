package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import jakarta.validation.constraints.NotBlank;

public record TitleDto(@NotBlank String value) {}
