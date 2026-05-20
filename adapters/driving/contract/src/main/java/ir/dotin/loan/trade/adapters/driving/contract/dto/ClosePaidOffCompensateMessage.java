package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Date;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.messaging.api.command.CommandPayload;
import ir.dotin.platform.messaging.api.version.ContractVersion;

@ContractVersion(1)
public record ClosePaidOffCompensateMessage(
        String producerCode,
        String eventUid,
        @Nullable Date dateTime,
        int version,
        @Nullable String responseTopic,
        @Nullable String[] tags,
        String operationType,
        String fileNumber,
        String transactionNumber,
        @Nullable String settleDate,
        @Nullable String settleReasonType,
        Map<String, Object> metadata)
        implements CommandPayload {}
