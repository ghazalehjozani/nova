package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Disbursement method enumeration")
public enum DisbursementMethodDto {
    @Schema(description = "Lump sum disbursement")
    LUMP_SUM,
    @Schema(description = "Staged regular disbursement")
    STAGED_REGULAR,
    @Schema(description = "Staged irregular disbursement")
    STAGED_IRREGULAR;

    public DisbursementMethod toDomain() {
        return switch (this) {
            case LUMP_SUM -> DisbursementMethod.LUMP_SUMP;
            case STAGED_REGULAR -> DisbursementMethod.STAGED_REGULAR;
            case STAGED_IRREGULAR -> DisbursementMethod.STAGED_IRREGULAR;
        };
    }
}
