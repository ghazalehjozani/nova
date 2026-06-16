package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.time.LocalDate;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.messaging.api.command.CommandPayload;

public record CancelFacilityMessage(
        String operationType,
        String fileNumber,
        LocalDate revokeDate,
        String revokeReason,
        String revokeDescription,
        @Nullable String revokeTransactionNumber,
        @Nullable String channel,
        Map<String, Object> metadata)
        implements CommandPayload {}
