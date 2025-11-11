package ir.dotin.loan.trade.core.domain.loanfacility.event;

public enum TradeLoanFacilityEventType {
    CREATED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    LUMP_SUM_DISBURSED,
    CONTRACT_ISSUED,
    COLLATERAL_ADDED,
    PARTIALLY_DISBURSED,
    ADDITIONAL_DISBURSEMENT_COMPLETED,
    IRREGULARLY_DISBURSED,
    DISBURSEMENT_FAILED,
    CLOSED_PAID_OFF,
    CLOSED_DEFAULTED,
    CANCELLED;

    private static final String PREFIX = "TRADE_LOAN_FACILITY_";

    public String getFullType() {
        return PREFIX + name();
    }
}
