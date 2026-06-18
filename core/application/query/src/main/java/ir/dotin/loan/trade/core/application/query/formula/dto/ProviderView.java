package ir.dotin.loan.trade.core.application.query.formula.dto;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.formula.api.spi.BindingRegistrar.ProviderInfo;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record ProviderView(@Nullable ProviderInfo provider) implements QueryResult {}
