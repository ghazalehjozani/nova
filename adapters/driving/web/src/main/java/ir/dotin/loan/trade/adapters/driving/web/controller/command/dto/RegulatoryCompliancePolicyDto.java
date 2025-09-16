package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Regulatory compliance policy configuration")
public class RegulatoryCompliancePolicyDto {

    @NotNull(message = "{regulatory.overdue.required}")
    @Min(value = 1, message = "{regulatory.overdue.negative}")
    @Max(value = 365, message = "{field.number.max}")
    @Schema(description = "Overdue period in days", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer overDuePeriodDays;

    @NotNull(message = "{regulatory.deferral.required}")
    @Min(value = 1, message = "{field.number.negative.not.allowed}")
    @Max(value = 365, message = "{field.number.max}")
    @Schema(description = "Deferral period in days", example = "60", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer deferralPeriodDays;

    @NotNull(message = "{regulatory.suspicious.required}")
    @Min(value = 1, message = "{field.number.negative.not.allowed}")
    @Max(value = 730, message = "{field.number.max}")
    @Schema(description = "Suspicious period in days", example = "90", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer suspiciousPeriodDays;
}
