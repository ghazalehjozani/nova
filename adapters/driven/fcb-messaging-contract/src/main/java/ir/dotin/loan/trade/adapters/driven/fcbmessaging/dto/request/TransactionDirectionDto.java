package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionDirectionDto {
    DEBTOR(true),
    CREDITOR(false);

    private final boolean debtor;
}
