package ir.dotin.loan.trade.core.application.ports.driven.dto;

import java.math.BigDecimal;

public record MoneyDto(BigDecimal amount, String currencyCode) {}
