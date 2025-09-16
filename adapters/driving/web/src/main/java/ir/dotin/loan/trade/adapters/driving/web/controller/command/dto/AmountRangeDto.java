package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Amount range specification with currency")
public class AmountRangeDto {

    @NotNull(message = "{field.required.amount}")
    @Positive(message = "{field.number.positive}")
    @DecimalMin(value = "1000", message = "{amount.min.too.low}")
    @Digits(integer = 12, fraction = 2, message = "{field.decimal.format}")
    @Schema(description = "Minimum loan amount", example = "1000000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal minAmount;

    @NotNull(message = "{field.required.amount}")
    @Positive(message = "{field.number.positive}")
    @DecimalMax(value = "999999999999", message = "{amount.max.exceeded}")
    @Digits(integer = 12, fraction = 2, message = "{field.decimal.format}")
    @Schema(description = "Maximum loan amount", example = "100000000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal maxAmount;

    @AssertTrue(message = "{amount.range.invalid}")
    @Schema(hidden = true)
    public boolean isRangeValid() {
        return minAmount == null || maxAmount == null || maxAmount.compareTo(minAmount) > 0;
    }
}
