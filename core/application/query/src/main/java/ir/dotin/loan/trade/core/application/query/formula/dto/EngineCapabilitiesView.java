package ir.dotin.loan.trade.core.application.query.formula.dto;

import ir.dotin.platform.formula.service.dto.EngineCapabilitiesDto;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record EngineCapabilitiesView(EngineCapabilitiesDto capabilities) implements QueryResult {}
