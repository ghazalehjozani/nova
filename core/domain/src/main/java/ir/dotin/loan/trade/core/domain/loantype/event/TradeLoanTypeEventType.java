package ir.dotin.loan.trade.core.domain.loantype.event;

public enum TradeLoanTypeEventType {
    CREATED,
    ACTIVATED,
    DEACTIVATED,
    VERSION_PREPARED;

    private static final String PREFIX = "TRADE_LOAN_TYPE_";

    public String getFullType() {
        return PREFIX + name();
    }
}
