package ir.dotin.loan.trade.core.domain.installmentschedule.event;

public enum InstallmentScheduleEventType {
    EQUAL_INSTALLMENTS_CREATED,
    GRADUAL_INSTALLMENTS_CREATED,
    ACTIVATED,
    CANCELLED,
    COMPLETED,
    ON_HOLD,
    RESTRUCTURED,
    STATE_TRANSITIONED;

    private static final String PREFIX = "INSTALLMENT_SCHEDULE_";

    public String getFullType() {
        return PREFIX + name();
    }
}
