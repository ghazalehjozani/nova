package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;

@Schema(description = "Penalty payment type")
public enum PenaltyPaymentTypeDto {
    @Schema(description = "Added to installment")
    ADDED_TO_INSTALLMENT,
    @Schema(description = "Separate payment")
    SEPARATE_PAYMENT,
    @Schema(description = "Deducted from principal")
    DEDUCTED_FROM_PRINCIPAL;

    public PenaltyPaymentType toDomain() {
        return switch (this) {
            case ADDED_TO_INSTALLMENT ->
                PenaltyPaymentType.INSTALLMENT_PENALTY_PAYMENT;
            case SEPARATE_PAYMENT, DEDUCTED_FROM_PRINCIPAL ->
                    PenaltyPaymentType.SETTLEMENT_PENALTY_PAYMENT;
        };
    }
}
