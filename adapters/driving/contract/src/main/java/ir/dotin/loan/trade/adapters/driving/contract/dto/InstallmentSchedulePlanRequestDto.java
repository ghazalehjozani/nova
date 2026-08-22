package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InstallmentSchedulePlanDto", description = "برنامه اقساط")
public record InstallmentSchedulePlanRequestDto(
        @Schema(description = "اطلاعات اقساط", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty
        List<@Valid InstallmentSpecRequestDto> installments) {}
