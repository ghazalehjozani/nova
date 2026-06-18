package ir.dotin.loan.trade.core.application.query.formula.dto;

import java.util.List;

import ir.dotin.platform.formula.api.spi.BindingRegistrar.ProviderInfo;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record ProviderListView(List<ProviderInfo> providers) implements QueryResult {}
