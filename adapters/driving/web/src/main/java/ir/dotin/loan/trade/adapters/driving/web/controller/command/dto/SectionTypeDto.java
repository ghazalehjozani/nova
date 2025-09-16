package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Section type")
public enum SectionTypeDto {
    @Schema(description = "Manufacturing")
    MANUFACTURING,
    @Schema(description = "Services")
    SERVICES,
    @Schema(description = "Commerce")
    COMMERCE,
    @Schema(description = "Agriculture")
    AGRICULTURE,
    @Schema(description = "Construction")
    CONSTRUCTION;

    public SectionType toDomain() {
        return switch (this) {
            case MANUFACTURING, CONSTRUCTION -> SectionType.FIXED;
            case SERVICES -> SectionType.CURRENT;
            case COMMERCE -> SectionType.FIXED_AND_CURRENT;
            case AGRICULTURE -> SectionType.NONE;
        };
    }
}
