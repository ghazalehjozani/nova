package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

import org.jspecify.annotations.Nullable;

public final class ApplicationNumberKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String fileNumber;

    public @Nullable String getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(@Nullable String fileNumber) {
        this.fileNumber = fileNumber;
    }
}
