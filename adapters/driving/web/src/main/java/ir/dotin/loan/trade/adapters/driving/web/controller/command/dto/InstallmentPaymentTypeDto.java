package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Installment payment type")
public enum InstallmentPaymentTypeDto {
    @Schema(description = "Equal installments")
    EQUAL,
    @Schema(description = "Decreasing installments")
    DECREASING,
    @Schema(description = "Custom schedule")
    CUSTOM;

    public ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType toDomain() {
        return switch (this) {
            case EQUAL -> ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType.SCHEDULED;
            case DECREASING -> ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType.GRADUAL;
            case CUSTOM -> ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType.ONE_TIME;
        };
    }
}
