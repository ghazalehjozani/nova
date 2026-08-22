package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driving.contract.dto.validation.Money;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoanApplicationDto", description = "اطلاعات درخواست تسهیلات")
public record LoanApplicationRequestDto(
        @Schema(description = "تاریخ درخواست", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Instant requestDate,

        @Schema(description = "ذینفعان شامل مشتری اصلی، فرعی و ضامنین", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        Set<@Valid PartyRequestDto> parties,

        @Schema(description = "مبلغ درخواستی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Money
        BigDecimal requestedAmount,

        @Schema(description = "روش پرداخت تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        DisbursementMethod disbursementMethod,

        @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String currency,

        @Schema(description = "مدت زمان تسهیلات (ماه)", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Integer requestedLoanDurationMonths,

        @Schema(description = "کانال متقاضی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        ApplicantChannel applicantChannel,

        @Schema(description = "دوره تنفس (روز)", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Integer gracePeriodDays,

        @Schema(description = "تعداد اقساط", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer installmentCount,

        @Schema(description = "مقصد پرداخت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        DisburseDestinationRequestDto disburseDestination,

        @Schema(description = "کد بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String economicSectorCode,

        @Schema(description = "کد دلیل درخواست", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String requestReasonCode,

        @Schema(description = "کد منبع فرعی", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String subSourceCode,

        @Schema(description = "توضیحات", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String description,

        @Schema(description = "شماره پرونده", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String applicationNumber,

        @Schema(description = "مقدار رتبه اعتباری", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String credibilityRank,

        @Schema(description = "اطلاعات مربوط به سمات", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        SamatRequestDto samat) {}
