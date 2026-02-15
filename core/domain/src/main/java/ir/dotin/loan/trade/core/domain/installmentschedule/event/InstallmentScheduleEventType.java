package ir.dotin.loan.trade.core.domain.installmentschedule.event;

public enum InstallmentScheduleEventType {
    EQUAL_INSTALLMENTS_CREATED,
    GRADUAL_INSTALLMENTS_CREATED,
    ACTIVATED,
    CANCELLED,
    COMPLETED,
    ON_HOLD,
    RESTRUCTURED,
    STATE_TRANSITIONED,
    CREATION_REVERTED,
    ACTIVATION_REVERTED,
    RESTRUCTURING_REVERTED,
    INSTALLMENT_CREATION_REVERTED,
    COMPLETION_REVERTED,
    INSTALLMENT_COLLECTED;

    private static final String PREFIX = "TRADE_LOAN_INSTALLMENT_SCHEDULE_";

    public String getFullType() {
        return PREFIX + name();
    }
}
