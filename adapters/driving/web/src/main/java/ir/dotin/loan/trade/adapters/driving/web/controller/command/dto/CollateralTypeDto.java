package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Collateral type specification")
public class CollateralTypeDto {

    @NotBlank(message = "{field.required.code}")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "{collateral.code.invalid}")
    @Size(max = 20, message = "{field.size.max.exceeded}")
    @Schema(description = "Collateral type code", example = "REAL_ESTATE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "{collateral.name.required}")
    @Size(min = 3, max = 100, message = "{field.size.invalid}")
    @Schema(
            description = "Collateral type name",
            example = "Real Estate Property",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Min(value = 0, message = "{field.number.negative.not.allowed}")
    @Max(value = 100, message = "{field.number.max}")
    @Schema(description = "Acceptable percentage of this collateral type", example = "80")
    private Integer acceptablePercentage;
}
