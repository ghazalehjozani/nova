package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

public final class ApplicationNumberKafkaResponse extends FcbKafkaBaseResponse {

    private @Nullable String fileNumber;

    public @Nullable String getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(@Nullable String fileNumber) {
        this.fileNumber = fileNumber;
    }
}
