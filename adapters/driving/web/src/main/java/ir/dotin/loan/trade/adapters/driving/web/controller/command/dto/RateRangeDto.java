package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
@Schema(description = "Rate range for preferential rates")
public class RateRangeDto {

    @NotNull(message = "{field.required}")
    @DecimalMin(value = "0", message = "{field.number.negative.not.allowed}")
    @DecimalMax(value = "100", message = "{field.number.max}")
    @Digits(integer = 3, fraction = 6, message = "{field.decimal.format}")
    @Schema(
            description = "Minimum preferential rate",
            example = "12.000000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal minRate;

    @NotNull(message = "{field.required}")
    @DecimalMin(value = "0", message = "{field.number.negative.not.allowed}")
    @DecimalMax(value = "100", message = "{field.number.max}")
    @Digits(integer = 3, fraction = 6, message = "{field.decimal.format}")
    @Schema(
            description = "Maximum preferential rate",
            example = "22.000000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal maxRate;

    @AssertTrue(message = "{interest.preferential.range.invalid}")
    @Schema(hidden = true)
    public boolean isRangeValid() {
        return minRate == null || maxRate == null || maxRate.compareTo(minRate) >= 0;
    }
}
