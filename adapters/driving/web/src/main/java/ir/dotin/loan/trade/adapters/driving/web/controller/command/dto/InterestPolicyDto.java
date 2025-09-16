package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.Valid;
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
@Schema(description = "Interest policy configuration")
public class InterestPolicyDto {

    @NotNull(message = "{interest.rate.required}")
    @DecimalMin(value = "0", message = "{interest.rate.negative}")
    @DecimalMax(value = "100", message = "{interest.rate.max.exceeded}")
    @Digits(integer = 3, fraction = 6, message = "{interest.rate.format.invalid}")
    @Schema(
            description = "Base annual interest rate percentage",
            example = "18.500000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal baseInterestRate;

    @NotNull(message = "{field.required}")
    @Valid
    @Schema(description = "Range for preferential interest rates", requiredMode = Schema.RequiredMode.REQUIRED)
    private RateRangeDto preferentialRateRange;

    @NotBlank(message = "{field.required}")
    @Size(max = 1000, message = "{field.size.max.exceeded}")
    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
    @Schema(
            description = "Interest calculation formula",
            example = "P * R * T / 365",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String interestFormula;

    @NotBlank(message = "{field.required}")
    @Size(max = 1000, message = "{field.size.max.exceeded}")
    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
    @Schema(description = "Refund interest calculation formula", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refundInterestFormula;

    @Schema(description = "Apply daily interest calculation", defaultValue = "false")
    private boolean dailyInterest = false;

    @Valid
    @Schema(description = "Formula field mappings")
    private Map<Character, FormulaFieldDto> fieldMappings;
}
