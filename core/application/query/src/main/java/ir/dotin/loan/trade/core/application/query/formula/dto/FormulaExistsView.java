package ir.dotin.loan.trade.core.application.query.formula.dto;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record FormulaExistsView(String code, boolean exists) implements QueryResult {}
