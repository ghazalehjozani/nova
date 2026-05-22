package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Date;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.messaging.api.command.CommandPayload;

public record CancelFacilityRequest(
        String producerCode,
        String eventUid,
        @Nullable Date dateTime,
        int version,
        @Nullable String responseTopic,
        @Nullable String[] tags,
        String operationType,
        String fileNumber,
        String revokeDate,
        String revokeReason,
        String revokeDescription,
        @Nullable String revokeTransactionNumber,
        @Nullable String channel,
        Map<String, Object> metadata)
        implements CommandPayload {}
