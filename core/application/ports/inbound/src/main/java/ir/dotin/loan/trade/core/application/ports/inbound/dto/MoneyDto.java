package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MoneyDto(
        @NotNull @DecimalMin(value = "0") BigDecimal value,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency) {}
