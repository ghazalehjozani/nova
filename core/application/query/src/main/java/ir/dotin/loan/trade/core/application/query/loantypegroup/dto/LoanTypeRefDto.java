package ir.dotin.loan.trade.core.application.query.loantypegroup.dto;

import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record LoanTypeRefDto(UUID loanTypeId, String code, String title, UUID groupId) implements QueryResult {}
