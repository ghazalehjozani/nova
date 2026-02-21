package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class ValidateEcoSectorRequest extends FcbKafkaBaseRequest {

    private final String economicalSectionCode;
    private final String loanTypeCode;

    public ValidateEcoSectorRequest(String economicalSectionCode, String loanTypeCode) {
        super("validate-ecoSection-loanType");
        this.economicalSectionCode = economicalSectionCode;
        this.loanTypeCode = loanTypeCode;
    }

    public String getEconomicalSectionCode() {
        return economicalSectionCode;
    }

    public String getLoanTypeCode() {
        return loanTypeCode;
    }
}
