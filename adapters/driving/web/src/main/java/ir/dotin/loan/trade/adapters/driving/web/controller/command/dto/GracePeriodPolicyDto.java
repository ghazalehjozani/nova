package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import jakarta.validation.constraints.AssertTrue;
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
@Schema(description = "Grace period policy configuration")
public class GracePeriodPolicyDto {

    @NotNull(message = "{grace.period.min.required}")
    @Min(value = 0, message = "{field.number.negative.not.allowed}")
    @Max(value = 365, message = "{field.number.max}")
    @Schema(description = "Minimum grace period in days", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer minGracePeriodDays;

    @NotNull(message = "{grace.period.max.required}")
    @Min(value = 0, message = "{field.number.negative.not.allowed}")
    @Max(value = 730, message = "{field.number.max}")
    @Schema(description = "Maximum grace period in days", example = "90", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxGracePeriodDays;

    @NotBlank(message = "{grace.period.formula.required}")
    @Size(max = 1000, message = "{field.size.max.exceeded}")
    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
    @Schema(description = "Grace period calculation formula", requiredMode = Schema.RequiredMode.REQUIRED)
    private String gracePeriodFormula;

    @AssertTrue(message = "{grace.period.range.invalid}")
    @Schema(hidden = true)
    public boolean isRangeValid() {
        return minGracePeriodDays == null || maxGracePeriodDays == null || maxGracePeriodDays >= minGracePeriodDays;
    }
}
