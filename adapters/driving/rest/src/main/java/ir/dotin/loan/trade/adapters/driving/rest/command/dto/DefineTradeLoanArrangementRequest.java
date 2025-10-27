package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DefineTradeLoanArrangementRequest", description = "درخواست ایجاد شرط اعطا")
public record DefineTradeLoanArrangementRequest(
        @Schema(
                        description = "شناسه یکتا",
                        requiredMode = Schema.RequiredMode.REQUIRED,
                        example = "e6c9b36b-0f57-4d9f-8d9b-9cbfdfc1bca1")
                @NotNull
                UUID uid,
        @Schema(description = "کد شرط اعطا", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                LoanArrangementCodeDto code,
        @Schema(description = "عنوان", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull TitleDto title,
        @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                CurrencyTypeDto currencyType,
        @Schema(description = "بازه مبلغی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                AmountRangeDto amountRange,
        @Schema(description = "بازه مدت زمان تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                LoanDurationRangeDto durationRange,
        @Schema(description = "نوع شخص", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull PartyType partyType,
        @Schema(description = "تأییدکننده", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                ConfirmTypeDto confirmType,
        @Schema(description = "تعداد ضامنین", requiredMode = Schema.RequiredMode.REQUIRED, example = "2") @NotNull
                Integer guarantorCount,
        @Schema(description = "روش پرداخت تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                DisbursementMethod disbursementMethod,
        @Schema(description = "اقساط کارت", requiredMode = Schema.RequiredMode.REQUIRED, example = "false") @NotNull
                Boolean hasInstallmentCard,
        @Schema(description = "روش پرداخت بیمه عمر", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                LifeInsurancePaymentType lifeInsurancePaymentType,
        @Schema(description = "نوع ثانویه تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                LoanSecondaryType loanSecondaryType,
        @Schema(description = "بخش", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull SectionType sectionType,
        @Schema(description = "آیا تأیید خودکار دارد؟", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
                @NotNull
                Boolean autoApproval,
        @Schema(description = "بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                EconomicSectorDto economicSector,
        @Schema(description = "سیاست نرخ سود", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                InterestPolicyDto interestPolicy,
        @Schema(description = "سیاست جریمه", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                PenaltyPolicyDto penaltyPolicy,
        @Schema(description = "سیاست اقساط", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                InstallmentPolicyDto installmentPolicy,
        @Schema(description = "سیاست دوره مهلت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                GracePeriodPolicyDto gracePeriodPolicy,
        @Schema(description = "سیاست اولویت بازپرداخت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                RepaymentPriorityPolicyDto repaymentPriorityPolicy,
        @Schema(description = "سیاست الزامات قانونی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,
        @Schema(description = "سیاست وثایق", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                CollateralPolicyDto collateralPolicy) {

    @Schema(name = "InterestPolicyDto", description = "سیاست نرخ سود")
    public record InterestPolicyDto(
            @Schema(description = "حداقل نرخ", example = "5.0") @NotBlank String minRate,
            @Schema(description = "حداکثر نرخ", example = "18.0") @NotBlank String maxRate,
            @Schema(description = "فرمول محاسبه سود") @NotBlank String interestFormula,
            @Schema(description = "فرمول بازگشت سود") @NotBlank String refundFormula,
            @Schema(description = "سود روزشمار", example = "true") @NotNull Boolean dailyInterest) {}

    @Schema(name = "PenaltyPolicyDto", description = "سیاست جریمه")
    public record PenaltyPolicyDto(
            @Schema(description = "نرخ جریمه", example = "6.5") @NotBlank String penaltyRate,
            @Schema(description = "نرخ سود تعویق", example = "3.0") @NotBlank String deferralInterestRate,
            @Schema(description = "فرمول جریمه") @NotBlank String formula,
            @Schema(description = "نوع پرداخت جریمه") @NotNull PenaltyPaymentType paymentType) {}

    @Schema(name = "InstallmentPolicyDto", description = "سیاست اقساط")
    public record InstallmentPolicyDto(
            @Schema(description = "دوره اقساط", example = "P1M") @NotNull Period installmentPeriod,
            @Schema(description = "فرمول محاسبه قسط") @NotBlank String installmentFormula,
            @Schema(description = "فرمول جزء سود قسط") @NotBlank String interestComponentFormula,
            @Schema(description = "نوع پرداخت اقساط") @NotNull InstallmentPaymentType paymentType,
            @Schema(description = "تعریف خودکار اقساط؟", example = "true") @NotNull
                    Boolean isDefineAutomaticInstallment) {}

    @Schema(name = "GracePeriodPolicyDto", description = "سیاست دوره مهلت")
    public record GracePeriodPolicyDto(
            @Schema(description = "حداقل روز مهلت", example = "10") @NotNull Integer minGracePeriodDays,
            @Schema(description = "حداکثر روز مهلت", example = "30") @NotNull Integer maxGracePeriodDays,
            @Schema(description = "فرمول محاسبه مهلت") @NotBlank String formula) {}

    @Schema(name = "RepaymentPriorityPolicyDto", description = "سیاست اولویت بازپرداخت")
    public record RepaymentPriorityPolicyDto(
            @NotNull Integer principalPriority,
            @NotNull Integer interestPriority,
            @NotNull Integer penaltyPriority,
            @NotNull Integer commissionPriority,
            @NotNull Integer insurancePriority,
            @NotNull Integer insurancePenaltyPriority,
            @NotNull Boolean hasEqualPriority) {}

    @Schema(name = "RegulatoryCompliancePolicyDto", description = "سیاست الزامات قانونی")
    public record RegulatoryCompliancePolicyDto(
            @NotNull Integer overDuePeriod, @NotNull Integer deferralPeriod, @NotNull Integer suspiciousPeriod) {}

    @Schema(name = "CollateralPolicyDto", description = "سیاست وثایق")
    public record CollateralPolicyDto(
            @Schema(description = "درصد کل وثایق", example = "120") @NotNull Integer totalPercent,
            @Schema(description = "انواع وثیقه") @NotNull Set<CollateralTypeDto> collateralTypes,
            @Schema(description = "انواع روش محاسبه وثیقه") @NotNull
                    CollateralCalculationType collateralCalculationType) {}

    @Schema(name = "CollateralTypeDto", description = "نوع وثیقه")
    public record CollateralTypeDto(@NotBlank @JsonProperty("code") String code) {}

    @Schema(name = "LoanArrangementCodeDto", description = "کد شرط اعطا")
    public record LoanArrangementCodeDto(@NotBlank @JsonProperty("value") String value) {}

    @Schema(name = "TitleDto", description = "عنوان")
    public record TitleDto(@NotBlank @JsonProperty("value") String value) {}

    @Schema(name = "CurrencyTypeDto", description = "نوع ارز")
    public record CurrencyTypeDto(@NotBlank @JsonProperty("value") String value) {}

    @Schema(name = "AmountRangeDto", description = "بازه مبلغی")
    public record AmountRangeDto(
            @NotNull @JsonProperty("min") MoneyDto min, @NotNull @JsonProperty("max") MoneyDto max) {}

    @Schema(name = "LoanDurationRangeDto", description = "بازه مدت زمان تسهیلات")
    public record LoanDurationRangeDto(
            @NotNull @JsonProperty("minDays") Period minDays, @NotNull @JsonProperty("maxDays") Period maxDays) {}

    @Schema(name = "MoneyDto", description = "مقدار پول")
    public record MoneyDto(@NotNull @JsonProperty("value") BigDecimal value) {}

    @Schema(name = "EconomicSectorDto", description = "بخش اقتصادی")
    public record EconomicSectorDto(@NotBlank @JsonProperty("code") String code) {}

    @Schema(name = "ConfirmTypeDto", description = "تأییدکننده")
    public record ConfirmTypeDto(@NotBlank @JsonProperty("personCode") String personCode) {}
}
