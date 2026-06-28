package ir.dotin.loan.trade.core.application.query.loantypegroup.dto;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryResult;

public record LoanTypeGroupNodeDto(
        UUID id, String code, String title, @Nullable UUID parentGroupId) implements QueryResult {}
