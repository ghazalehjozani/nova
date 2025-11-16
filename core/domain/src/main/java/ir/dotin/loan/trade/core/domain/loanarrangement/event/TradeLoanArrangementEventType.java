package ir.dotin.loan.trade.core.domain.loanarrangement.event;

public enum TradeLoanArrangementEventType {
    CREATED,
    ACTIVATED,
    DEACTIVATED,
    VERSION_PREPARED;

    private static final String PREFIX = "TRADE_LOAN_ARRANGEMENT_";

    public String getFullType() {
        return PREFIX + name();
    }
}
