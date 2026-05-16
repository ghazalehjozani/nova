package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.messaging.api.command.CommandPayload;

public record CollateralUpdateMessage(
        String producerCode,
        String eventUid,
        @Nullable Date dateTime,
        int version,
        @Nullable String[] tags,
        String operationType,
        String fileNumber,
        List<CollateralDetailDto> collaterals,
        @Nullable String channel,
        Map<String, Object> metadata)
        implements CommandPayload {

    public record CollateralDetailDto(String collateralSerial, BigDecimal usedAmount) {}
}
