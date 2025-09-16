package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Penalty policy configuration")
public class PenaltyPolicyDto {

    @NotNull(message = "{penalty.rate.required}")
    @DecimalMin(value = "0", message = "{field.number.negative.not.allowed}")
    @DecimalMax(value = "100", message = "{penalty.rate.invalid}")
    @Digits(integer = 3, fraction = 6, message = "{field.decimal.format}")
    @Schema(
            description = "Annual penalty rate percentage",
            example = "24.000000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal penaltyRate;

    @NotNull(message = "{field.required}")
    @DecimalMin(value = "0", message = "{field.number.negative.not.allowed}")
    @DecimalMax(value = "100", message = "{field.number.max}")
    @Digits(integer = 3, fraction = 6, message = "{field.decimal.format}")
    @Schema(
            description = "Deferral interest rate percentage",
            example = "20.000000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal deferralInterestRate;

    @NotBlank(message = "{penalty.formula.required}")
    @Size(max = 1000, message = "{penalty.formula.too.long}")
    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
    @Schema(description = "Penalty calculation formula", requiredMode = Schema.RequiredMode.REQUIRED)
    private String penaltyFormula;

    @NotNull(message = "{penalty.type.required}")
    @Schema(description = "How penalties are applied", requiredMode = Schema.RequiredMode.REQUIRED)
    private PenaltyPaymentTypeDto penaltyPaymentType;
}
