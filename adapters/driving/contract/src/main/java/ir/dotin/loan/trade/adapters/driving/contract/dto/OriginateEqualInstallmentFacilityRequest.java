package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "OriginateEqualInstallmentFacilityRequest",
        description = "درخواست ایجاد تسهیلات با اقساط مساوی یا یکجا — جدول اقساط توسط سامانه تولید می‌شود")
public record OriginateEqualInstallmentFacilityRequest(
        @Schema(description = "کد نوع تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String loanTypeCode,

        @Schema(description = "کد شرایط تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String loanArrangementCode,

        @Schema(description = "درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        LoanApplicationRequestDto loanApplication,

        Map<String, String> metadata)
        implements BaseRequest {}
