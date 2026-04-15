package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class ValidateSamatRequest extends FcbKafkaBaseRequest {

    private final String trackingNumber;
    private final String isicEconomicSector;
    private final String subIsicEconomicSector;
    private final String useType;
    private final String exceptionCode;
    private final String consumptionPlaceCode;
    private final String loanTypeCode;
    private final String economicalSectionCode;

    public static Builder builder() {
        return new Builder();
    }

    public ValidateSamatRequest(
            String trackingNumber,
            String isicEconomicSector,
            String subIsicEconomicSector,
            String useType,
            String exceptionCode,
            String consumptionPlaceCode,
            String loanTypeCode,
            String economicalSectionCode) {
        super("validate-samat");
        this.trackingNumber = trackingNumber;
        this.isicEconomicSector = isicEconomicSector;
        this.subIsicEconomicSector = subIsicEconomicSector;
        this.useType = useType;
        this.exceptionCode = exceptionCode;
        this.consumptionPlaceCode = consumptionPlaceCode;
        this.loanTypeCode = loanTypeCode;
        this.economicalSectionCode = economicalSectionCode;
    }

    public ValidateSamatRequest(Builder builder) {
        super("validate-samat");
        this.trackingNumber = builder.trackingNumber;
        this.isicEconomicSector = builder.isicEconomicSector;
        this.subIsicEconomicSector = builder.subIsicEconomicSector;
        this.useType = builder.useType;
        this.exceptionCode = builder.exceptionCode;
        this.consumptionPlaceCode = builder.consumptionPlaceCode;
        this.loanTypeCode = builder.loanTypeCode;
        this.economicalSectionCode = builder.economicalSectionCode;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getIsicEconomicSector() {
        return isicEconomicSector;
    }

    public String getSubIsicEconomicSector() {
        return subIsicEconomicSector;
    }

    public String getUseType() {
        return useType;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public String getConsumptionPlaceCode() {
        return consumptionPlaceCode;
    }

    public String getLoanTypeCode() {
        return loanTypeCode;
    }

    public String getEconomicalSectionCode() {
        return economicalSectionCode;
    }

    public static final class Builder {
        private String trackingNumber;
        private String isicEconomicSector;
        private String subIsicEconomicSector;
        private String useType;
        private String exceptionCode;
        private String consumptionPlaceCode;
        private String loanTypeCode;
        private String economicalSectionCode;

        private Builder() {}

        public Builder withTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
            return this;
        }

        public Builder withIsicEconomicSector(String isicEconomicSector) {
            this.isicEconomicSector = isicEconomicSector;
            return this;
        }

        public Builder withSubIsicEconomicSector(String subIsicEconomicSector) {
            this.subIsicEconomicSector = subIsicEconomicSector;
            return this;
        }

        public Builder withUseType(String useType) {
            this.useType = useType;
            return this;
        }

        public Builder withExceptionCode(String exceptionCode) {
            this.exceptionCode = exceptionCode;
            return this;
        }

        public Builder withConsumptionPlaceCode(String consumptionPlaceCode) {
            this.consumptionPlaceCode = consumptionPlaceCode;
            return this;
        }

        public Builder withLoanTypeCode(String loanTypeCode) {
            this.loanTypeCode = loanTypeCode;
            return this;
        }

        public Builder withEconomicalSectionCode(String economicalSectionCode) {
            this.economicalSectionCode = economicalSectionCode;
            return this;
        }

        public ValidateSamatRequest build() {
            return new ValidateSamatRequest(
                    trackingNumber,
                    isicEconomicSector,
                    subIsicEconomicSector,
                    useType,
                    exceptionCode,
                    consumptionPlaceCode,
                    loanTypeCode,
                    economicalSectionCode);
        }
    }
}
