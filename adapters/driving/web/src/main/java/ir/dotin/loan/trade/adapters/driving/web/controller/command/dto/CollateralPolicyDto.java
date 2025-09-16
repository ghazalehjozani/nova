package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Collateral policy configuration")
public class CollateralPolicyDto {

    @NotEmpty(message = "{collateral.types.required}")
    @Size(max = 20, message = "{collateral.types.max.exceeded}")
    @Valid
    @Schema(description = "Accepted collateral types", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<CollateralTypeDto> collateralTypes;

    @NotNull(message = "{field.required}")
    @Min(value = 100, message = "{collateral.percent.min}")
    @Max(value = 500, message = "{collateral.percent.max}")
    @Schema(
            description = "Total collateral coverage percentage",
            example = "150",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalPercent;
}
