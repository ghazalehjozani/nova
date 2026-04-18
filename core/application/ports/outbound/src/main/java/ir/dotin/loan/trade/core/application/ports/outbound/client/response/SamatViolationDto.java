package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import java.util.Objects;

public class SamatViolationDto {

    private String field;
    private String providedValue;
    private String violationCode;
    private String message;
    private ViolationSeverity severity;
    private ViolationCategory category;

    public enum ViolationSeverity {
        ERROR,
        WARNING
    }

    public enum ViolationCategory {
        USE_TYPE,
        ISIC_ECONOMIC_SECTOR,
        SUB_ISIC_ECONOMIC_SECTOR,
        EXCEPTION_CODE,
        CONSUMPTION_PLACE_CODE,
        ECONOMICAL_SECTION
    }

    public SamatViolationDto() {}

    private SamatViolationDto(Builder builder) {
        this.field = builder.field;
        this.providedValue = builder.providedValue;
        this.violationCode = builder.violationCode;
        this.message = builder.message;
        this.severity = builder.severity;
        this.category = builder.category;
    }

    public String getField() {
        return field;
    }

    public String getProvidedValue() {
        return providedValue;
    }

    public String getViolationCode() {
        return violationCode;
    }

    public String getMessage() {
        return message;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public ViolationCategory getCategory() {
        return category;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String field;
        private String providedValue;
        private String violationCode;
        private String message;
        private ViolationSeverity severity = ViolationSeverity.ERROR;
        private ViolationCategory category;

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder providedValue(String providedValue) {
            this.providedValue = providedValue;
            return this;
        }

        public Builder violationCode(String violationCode) {
            this.violationCode = violationCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder severity(ViolationSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder category(ViolationCategory category) {
            this.category = category;
            return this;
        }

        public SamatViolationDto build() {
            Objects.requireNonNull(field, "field must not be null");
            Objects.requireNonNull(violationCode, "violationCode must not be null");
            Objects.requireNonNull(category, "category must not be null");
            return new SamatViolationDto(this);
        }
    }

    @Override
    public String toString() {
        return "ViolationDto{" + "field='"
                + field + '\'' + ", providedValue='"
                + providedValue + '\'' + ", violationCode='"
                + violationCode + '\'' + ", message='"
                + message + '\'' + ", severity="
                + severity + ", category="
                + category + '}';
    }
}
