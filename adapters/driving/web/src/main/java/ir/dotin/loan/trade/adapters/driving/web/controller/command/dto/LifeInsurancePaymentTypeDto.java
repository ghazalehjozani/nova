package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Life insurance payment type")
public enum LifeInsurancePaymentTypeDto {
    @Schema(description = "No insurance")
    NONE,
    @Schema(description = "Upfront payment")
    UPFRONT,
    @Schema(description = "Periodic payment")
    PERIODIC,
    @Schema(description = "Deducted from loan")
    DEDUCTED
}
