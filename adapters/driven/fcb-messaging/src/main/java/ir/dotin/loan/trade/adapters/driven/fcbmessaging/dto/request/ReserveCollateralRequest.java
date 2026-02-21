package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.math.BigDecimal;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;

public final class ReserveCollateralRequest extends FcbKafkaBaseRequest {

    private final String assuranceSerial;
    private final String fileNumber;
    private final String transactionId;
    private final int reserveDurationMin;
    private final BigDecimal amount;

    public ReserveCollateralRequest(
            String assuranceSerial,
            String fileNumber,
            String transactionId,
            int reserveDurationMin,
            BigDecimal amount) {
        super("reserve-assurance-for-file");
        this.assuranceSerial = assuranceSerial;
        this.fileNumber = fileNumber;
        this.transactionId = transactionId;
        this.reserveDurationMin = reserveDurationMin;
        this.amount = amount;
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

    public int getReserveDurationMin() {
        return reserveDurationMin;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
