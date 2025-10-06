package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import java.math.BigDecimal;

public record MoneyDto(BigDecimal amount, String currencyCode) {}
