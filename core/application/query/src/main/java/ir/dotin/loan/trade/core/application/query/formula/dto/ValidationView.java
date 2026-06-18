package ir.dotin.loan.trade.core.application.query.formula.dto;

import ir.dotin.platform.formula.service.dto.ValidationResultDto;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record ValidationView(ValidationResultDto result) implements QueryResult {}
