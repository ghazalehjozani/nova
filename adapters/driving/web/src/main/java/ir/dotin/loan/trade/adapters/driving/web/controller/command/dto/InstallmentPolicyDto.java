package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Installment policy configuration")
public class InstallmentPolicyDto {

    @NotNull(message = "{installment.period.required}")
    @Min(value = 1, message = "{field.number.min}")
    @Max(value = 365, message = "{installment.period.invalid}")
    @Schema(description = "Installment period in days", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer installmentPeriodDays;

    @NotBlank(message = "{installment.formula.required}")
    @Size(max = 1000, message = "{field.size.max.exceeded}")
    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
    @Schema(description = "Installment calculation formula", requiredMode = Schema.RequiredMode.REQUIRED)
    private String installmentFormula;

    @NotBlank(message = "{field.required}")
    @Size(max = 1000, message = "{field.size.max.exceeded}")
    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
    @Schema(description = "Interest component formula", requiredMode = Schema.RequiredMode.REQUIRED)
    private String interestComponentFormula;

    @NotNull(message = "{installment.payment.type.required}")
    @Schema(description = "Installment payment type", requiredMode = Schema.RequiredMode.REQUIRED)
    private InstallmentPaymentTypeDto installmentPaymentType;

    @Schema(description = "Define automatic installment schedule", defaultValue = "true")
    private boolean defineAutomaticInstallment = true;
}
