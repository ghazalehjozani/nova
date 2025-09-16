package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Loan secondary type")
public enum LoanSecondaryTypeDto {
    @Schema(description = "Working capital")
    WORKING_CAPITAL,
    @Schema(description = "Fixed assets")
    FIXED_ASSETS,
    @Schema(description = "Trade finance")
    TRADE_FINANCE,
    @Schema(description = "Export finance")
    EXPORT_FINANCE,
    @Schema(description = "Import finance")
    IMPORT_FINANCE;

    public LoanSecondaryType toDomain() {
        return switch (this) {
            case WORKING_CAPITAL, IMPORT_FINANCE -> LoanSecondaryType.GENERAL;
            case FIXED_ASSETS -> LoanSecondaryType.SPECIFIC;
            case TRADE_FINANCE -> LoanSecondaryType.GENERAL_AND_SPECIFIC;
            case EXPORT_FINANCE -> LoanSecondaryType.NONE;
        };
    }
}
