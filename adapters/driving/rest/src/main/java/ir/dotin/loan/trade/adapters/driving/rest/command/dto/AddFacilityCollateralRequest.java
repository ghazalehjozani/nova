package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AddFacilityCollateralRequest", description = "عملیات مدیریت وثایق تسهیلات")
public record AddFacilityCollateralRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                Integer version,
        @Schema(description = "مبلغ مورد استفاده از وثیقه", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
                MoneyDto usedAmount) {

    public record MoneyDto(
            @Schema(description = "مبلغ", example = "1000000") @NotNull BigDecimal value,
            @Schema(description = "کد ارز", example = "IRR") @NotBlank String currency) {}
}
