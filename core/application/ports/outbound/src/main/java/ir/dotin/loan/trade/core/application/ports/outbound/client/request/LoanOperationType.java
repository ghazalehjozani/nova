package ir.dotin.loan.trade.core.application.ports.outbound.client.request;

import ir.dotin.platform.commons.core.i18n.LocalizedEnum;

public enum LoanOperationType implements LocalizedEnum<LoanOperationType> {
    RECEIVE_LOAN,
    ISSUE_SANCTION,
    REVOKE_CONTRACT,
    NOT_APPROVE;
}
