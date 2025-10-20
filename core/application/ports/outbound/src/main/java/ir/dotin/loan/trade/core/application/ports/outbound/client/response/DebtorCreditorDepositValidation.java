package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;

public record DebtorCreditorDepositValidation(Direction direction) {}
