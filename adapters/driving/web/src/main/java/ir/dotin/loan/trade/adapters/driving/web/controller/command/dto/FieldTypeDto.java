package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Field type for formulas")
public enum FieldTypeDto {
    @Schema(description = "Money/Amount field")
    MONEY,
    @Schema(description = "Rate/Percentage field")
    RATE,
    @Schema(description = "Duration field")
    DURATION,
    @Schema(description = "Integer field")
    INTEGER,
    @Schema(description = "Decimal field")
    DECIMAL
}
