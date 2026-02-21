package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class UnReserveCollateralRequest extends FcbKafkaBaseRequest {

    private final String assuranceSerial;
    private final String fileNumber;
    private final String transactionId;
    private final String rollBackId;

    public UnReserveCollateralRequest(
            String assuranceSerial, String fileNumber, String transactionId, String rollBackId) {
        super("un-reserve-assurance-for-file");
        this.assuranceSerial = assuranceSerial;
        this.fileNumber = fileNumber;
        this.transactionId = transactionId;
        this.rollBackId = rollBackId;
    }

    public String getAssuranceSerial() {
        return assuranceSerial;
    }

    public String getFileNumber() {
        return fileNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getRollBackId() {
        return rollBackId;
    }
}
