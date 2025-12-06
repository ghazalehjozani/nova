package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record CompensationRequest(@NotNull Long version, String reason, UUID installmentScheduleId) {}
