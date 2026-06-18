package ir.dotin.loan.trade.core.application.query.formula.dto;

import java.util.Set;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record BindingNamesView(Set<String> bindingNames) implements QueryResult {}
