package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.List;
import jakarta.validation.constraints.NotNull;

public record CompensateCollateralRequest(@NotNull Integer version, List<String> collateralSerials) {}
