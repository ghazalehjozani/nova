package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.time.LocalDate;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.messaging.api.command.CommandPayload;

public record ClosePaidOffCompensateMessage(
        String operationType,
        String fileNumber,
        String transactionNumber,
        @Nullable LocalDate settleDate,
        @Nullable String settleReasonType,
        Map<String, Object> metadata)
        implements CommandPayload {}
