package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AmountDto(@NotNull @DecimalMin(value = "0") BigDecimal value) {}
