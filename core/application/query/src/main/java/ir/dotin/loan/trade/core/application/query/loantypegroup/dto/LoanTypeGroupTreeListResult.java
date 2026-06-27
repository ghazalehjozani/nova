package ir.dotin.loan.trade.core.application.query.loantypegroup.dto;

import java.util.List;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record LoanTypeGroupTreeListResult(List<LoanTypeGroupTreeDto> items) implements QueryResult {}
