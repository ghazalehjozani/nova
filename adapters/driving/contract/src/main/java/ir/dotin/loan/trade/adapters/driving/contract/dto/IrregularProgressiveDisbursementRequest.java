package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IrregularProgressiveDisbursementRequest", description = "درخواست پرداخت نامنظم تسهیلات")
public record IrregularProgressiveDisbursementRequest(
        @Schema(description = "مبلغ پرداخت") @NotNull BigDecimal trancheAmount,
        @Schema(description = "برنامه اقساط") @NotNull InstallmentSchedulePlanDto installmentSchedulePlan,
        @Schema(description = "شناسه عملیات") @NotNull UUID uid,
        @Schema(description = "نسخه عملیات") @NotNull Long version,
        @Schema(description = "تاریخ پرداخت") LocalDate disbursementDate,
        Map<String, String> metadata)
        implements BaseRequest {

    @Schema(description = "برنامه زمانبندی اقساط")
    public record InstallmentSchedulePlanDto(
            @Schema(description = "لیست اقساط") @NotNull @Valid
            List<InstallmentSpecDto> installments) {}

    @Schema(description = "مشخصات قسط")
    public record InstallmentSpecDto(
            @Schema(description = "شماره ترتیب قسط") @NotNull
            Integer sequenceNumber,

            @Schema(description = "تاریخ سررسید قسط") @NotNull
            LocalDate dueDate,

            @Schema(description = "مبلغ اصل قسط") @NotNull BigDecimal principalAmount,
            @Schema(description = "مبلغ سود قسط") @NotNull BigDecimal interestAmount) {}
}
