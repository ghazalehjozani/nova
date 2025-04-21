package ir.dotin.loan.morabehe.core.domain;

public final class MorabeheLoanValidationConstants {

    private MorabeheLoanValidationConstants() {}

    public static final String VALIDATION_PREFIX = "validation.";
    public static final String MODULE_NAME = "morabehe";

    public static final String LOAN_RULE_AGGREGATE_NAME = "loanrule";
    public static final String LOAN_TYPE_AGGREGATE_NAME = "loantype";
    public static final String LOAN_APPLICATION_AGGREGATE_NAME = "loanapplication";

    public static final String LOAN_RULE_KEY_PREFIX =
            VALIDATION_PREFIX + MODULE_NAME + "." + LOAN_RULE_AGGREGATE_NAME + ".";

    public static final String LOAN_TYPE_KEY_PREFIX =
            VALIDATION_PREFIX + MODULE_NAME + "." + LOAN_TYPE_AGGREGATE_NAME + ".";

    public static final String LOAN_APPLICATION_KEY_PREFIX =
            VALIDATION_PREFIX + MODULE_NAME + "." + LOAN_APPLICATION_AGGREGATE_NAME + ".";
}
