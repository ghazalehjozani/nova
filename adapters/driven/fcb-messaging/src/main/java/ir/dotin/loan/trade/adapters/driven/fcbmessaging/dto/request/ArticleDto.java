package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.math.BigDecimal;

public record ArticleDto(String targetType, String targetId, BigDecimal amount, String currency, boolean isDebtor) {}
