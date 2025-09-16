package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import jakarta.validation.constraints.AssertTrue;
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
@Schema(description = "Duration range specification in days")
public class DurationRangeDto {

    @NotNull(message = "{field.required}")
    @Min(value = 1, message = "{duration.min.days}")
    @Max(value = 7300, message = "{duration.max.days}")
    @Schema(description = "Minimum loan duration in days", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer minDurationDays;

    @NotNull(message = "{field.required}")
    @Min(value = 1, message = "{duration.min.days}")
    @Max(value = 7300, message = "{duration.max.years.exceeded}")
    @Schema(
            description = "Maximum loan duration in days",
            example = "1825",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxDurationDays;

    @AssertTrue(message = "{duration.range.invalid}")
    @Schema(hidden = true)
    public boolean isDurationRangeValid() {
        return minDurationDays == null || maxDurationDays == null || maxDurationDays > minDurationDays;
    }
}
