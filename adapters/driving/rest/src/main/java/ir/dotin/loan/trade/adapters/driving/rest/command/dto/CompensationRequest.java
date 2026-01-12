package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.protocol.api.request.BaseRequest;

public record CompensationRequest(
        @NotNull Long version, String reason, UUID installmentScheduleId, Map<String, String> metadata)
        implements BaseRequest {}
