package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoanArrangementCodeDto(
        @NotBlank @Pattern(regexp = "^\\d+$") String value) {}
