package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "OriginateUnequalInstallmentFacilityRequest",
        description = "درخواست ایجاد تسهیلات با اقساط نامساوی — جدول اقساط توسط کاربر ارسال می‌شود")
public record OriginateUnequalInstallmentFacilityRequest(
        @Schema(description = "کد نوع تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String loanTypeCode,

        @Schema(description = "کد شرایط تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String loanArrangementCode,

        @Schema(description = "درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        LoanApplicationRequestDto loanApplication,

        @Schema(description = "برنامه اقساط", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        InstallmentSchedulePlanRequestDto installmentSchedulePlan,

        Map<String, String> metadata)
        implements BaseRequest {}
