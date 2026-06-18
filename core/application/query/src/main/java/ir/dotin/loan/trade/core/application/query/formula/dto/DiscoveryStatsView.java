package ir.dotin.loan.trade.core.application.query.formula.dto;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record DiscoveryStatsView(int providerCount, int bindingCount) implements QueryResult {}
