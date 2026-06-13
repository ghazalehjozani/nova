package ir.dotin.loan.trade.core.application.query.loanfacility.dto;

import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record FacilityIdView(UUID loanFacilityId) implements QueryResult {}
