package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ir.dotin.platform.protocol.api.request.BaseRequest;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OriginateLoanFacilityRequest", description = "درخواست ایجاد تسهیلات")
public record OriginateLoanFacilityRequest(
        @Schema(description = "کد نوع تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
        String loanTypeCode,

        @Schema(description = "کد شرایط تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
        String loanArrangementCode,

        @Schema(description = "درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
        LoanApplicationDto loanApplication,

        @Schema(description = "برنامه اقساط", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        InstallmentSchedulePlanDto installmentSchedulePlan,

        Map<String, String> metadata)
        implements BaseRequest {
    @Schema(name = "LoanApplicationDto", description = "اطلاعات درخواست تسهیلات")
    public record LoanApplicationDto(
            @Schema(description = "تاریخ درخواست", requiredMode = Schema.RequiredMode.REQUIRED)
            Instant requestDate,

            @Schema(description = "ذینفعان شامل مشتری اصلی، فرعی و ضامنین", requiredMode = Schema.RequiredMode.REQUIRED)
            Set<PartyRequestDto> parties,

            @Schema(description = "مبلغ درخواستی", requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal requestedAmount,

            @Schema(description = "روش پرداخت تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
            DisbursementMethod disbursementMethod,

            @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED)
            String currency,

            @Schema(description = "مدت زمان تسهیلات (ماه)", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer requestedLoanDurationMonths,

            @Schema(description = "کانال متقاضی", requiredMode = Schema.RequiredMode.REQUIRED)
            ApplicantChannel applicantChannel,

            @Schema(description = "دوره تنفس (روز)", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer gracePeriodDays,

            @Schema(description = "تعداد اقساط", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            Integer installmentCount,

            @Schema(description = "مقصد پرداخت", requiredMode = Schema.RequiredMode.REQUIRED)
            DisburseDestinationRequestDto disburseDestination,

            @Schema(description = "کد بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED)
            String economicSectorCode,

            @Schema(description = "کد دلیل درخواست", requiredMode = Schema.RequiredMode.REQUIRED)
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
            SamatDto samat) {}

    @Schema(name = "SamatDto", description = "اطلاعات مربوط به سمات")
    public record SamatDto(
            @Schema(description = "شماره پیگیری سمات", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String trackingNumber,

            @Schema(description = "بخش اقتصادی isic", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String isicEconomicSector,

            @Schema(description = "زیر بخش اقتصادی isic", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String subIsicEconomicSector,

            @Schema(description = "نوع استفاده", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String useType,

            @Schema(description = "کد استثنا", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String exceptionCode,

            @Schema(description = "شهر محل مصرف", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            String consumptionPlaceCode) {}

    @Schema(name = "InstallmentSchedulePlanDto", description = "برنامه اقساط")
    public record InstallmentSchedulePlanDto(
            @Schema(description = "اطلاعات اقساط", requiredMode = Schema.RequiredMode.REQUIRED)
            List<InstallmentSpecDto> installments) {}

    @Schema(name = "InstallmentSpecDto", description = "مشخصات قسط")
    public record InstallmentSpecDto(
            @Schema(description = "شماره ترتیب قسط", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer sequenceNumber,

            @Schema(description = "سررسید قسط", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDate dueDate,

            @Schema(description = "مبلغ اصل", requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal principalAmount,

            @Schema(description = "مبلغ سود", requiredMode = Schema.RequiredMode.REQUIRED)
            BigDecimal interestAmount,

            @Schema(description = "مبلغ جریمه", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            BigDecimal penaltyAmount,

            @Schema(description = "مبلغ کارمزد", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            BigDecimal feeAmount) {}
}
