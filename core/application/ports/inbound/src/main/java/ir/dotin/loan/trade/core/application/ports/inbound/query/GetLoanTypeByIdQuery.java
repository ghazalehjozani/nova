package ir.dotin.loan.trade.core.application.ports.inbound.query;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.query.Query;

import lombok.Builder;

@Builder
public record GetLoanTypeByIdQuery(UUID uid, UUID loanTypeId) implements Query {}
