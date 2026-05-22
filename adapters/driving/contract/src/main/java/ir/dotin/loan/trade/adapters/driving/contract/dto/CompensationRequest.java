package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

public record CompensationRequest(
        @NotNull Long version, String reason, UUID installmentScheduleId, Map<String, String> metadata)
        implements BaseRequest {}
