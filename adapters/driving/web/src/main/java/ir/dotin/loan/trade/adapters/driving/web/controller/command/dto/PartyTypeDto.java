package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Party type enumeration")
public enum PartyTypeDto {
    @Schema(description = "Individual customer")
    INDIVIDUAL,
    @Schema(description = "Legal entity customer")
    LEGAL
}
