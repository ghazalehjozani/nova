package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MoneyDto(@NotNull @Positive BigDecimal value, String currency) {}
