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
                @NotNull(message = "شناسه یکتا الزامی است.")
                @JsonProperty("uid")
                UUID uid,
        @Schema(description = "کد شرط اعطا", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "کد شرط اعطا الزامی است.")
                @JsonProperty("code")
                LoanArrangementCodeDto code,
        @Schema(description = "عنوان", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "عنوان الزامی است.")
                @JsonProperty("title")
                TitleDto title,
        @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "نوع ارز الزامی است.")
                @JsonProperty("currencyType")
                CurrencyTypeDto currencyType,
        @Schema(description = "بازه مبلغی", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "بازه مبلغی الزامی است.")
                @JsonProperty("amountRange")
                AmountRangeDto amountRange,
        @Schema(description = "بازه مدت زمان تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "بازه مدت زمان الزامی است.")
                @JsonProperty("durationRange")
                LoanDurationRangeDto durationRange,
        @Schema(description = "نوع شخص", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "نوع شخص الزامی است.")
                @JsonProperty("partyType")
                PartyType partyType,
        @Schema(description = "تأییدکننده", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "تأییدکننده الزامی است.")
                @JsonProperty("confirmType")
                ConfirmTypeDto confirmType,
        @Schema(description = "تعداد ضامنین", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
                @NotNull(message = "تعداد ضامنین الزامی است.")
                @JsonProperty("guarantorCount")
                Integer guarantorCount,
        @Schema(description = "روش پرداخت تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "روش پرداخت تسهیلات الزامی است.")
                @JsonProperty("disbursementMethod")
                DisbursementMethod disbursementMethod,
        @Schema(description = "اقساط کارت", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
                @NotNull(message = "اقساط کارت الزامی است.")
                @JsonProperty("hasInstallmentCard")
                Boolean hasInstallmentCard,
        @Schema(description = "روش پرداخت بیمه عمر", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "روش پرداخت بیمه عمر الزامی است.")
                @JsonProperty("lifeInsurancePaymentType")
                LifeInsurancePaymentType lifeInsurancePaymentType,
        @Schema(description = "نوع ثانویه تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "نوع ثانویه الزامی است.")
                @JsonProperty("loanSecondaryType")
                LoanSecondaryType loanSecondaryType,
        @Schema(description = "بخش", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "بخش الزامی است.")
                @JsonProperty("sectionType")
                SectionType sectionType,
        @Schema(description = "آیا تأیید خودکار دارد؟", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
                @NotNull(message = "تأیید خودکار الزامی است.")
                @JsonProperty("autoApproval")
                Boolean autoApproval,
        @Schema(description = "بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "بخش اقتصادی الزامی است.")
                @JsonProperty("economicSector")
                EconomicSectorDto economicSector,
        @Schema(description = "سیاست نرخ سود", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "سیاست نرخ سود الزامی است.")
                @JsonProperty("interestPolicy")
                InterestPolicyDto interestPolicy,
        @Schema(description = "سیاست جریمه", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "سیاست جریمه الزامی است.")
                @JsonProperty("penaltyPolicy")
                PenaltyPolicyDto penaltyPolicy,
        @Schema(description = "سیاست اقساط", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "سیاست اقساط الزامی است.")
                @JsonProperty("installmentPolicy")
                InstallmentPolicyDto installmentPolicy,
        @Schema(description = "سیاست دوره مهلت", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "سیاست دوره مهلت الزامی است.")
                @JsonProperty("gracePeriodPolicy")
                GracePeriodPolicyDto gracePeriodPolicy,
        @Schema(description = "سیاست اولویت بازپرداخت", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "سیاست اولویت بازپرداخت الزامی است.")
                @JsonProperty("repaymentPriorityPolicy")
                RepaymentPriorityPolicyDto repaymentPriorityPolicy,
        @Schema(description = "سیاست الزامات قانونی", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "سیاست الزامات قانونی الزامی است.")
                @JsonProperty("regulatoryCompliancePolicy")
                RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,
        @Schema(description = "سیاست وثایق", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "سیاست وثایق الزامی است.")
                @JsonProperty("collateralPolicy")
                CollateralPolicyDto collateralPolicy) {

    @Schema(name = "InterestPolicyDto", description = "سیاست نرخ سود")
    public record InterestPolicyDto(
            @Schema(description = "حداقل نرخ", example = "5.0") @NotBlank @JsonProperty("minRate") String minRate,
            @Schema(description = "حداکثر نرخ", example = "18.0") @NotBlank @JsonProperty("maxRate") String maxRate,
            @Schema(description = "فرمول محاسبه سود") @NotBlank @JsonProperty("interestFormula") String interestFormula,
            @Schema(description = "فرمول بازگشت سود") @NotBlank @JsonProperty("refundFormula") String refundFormula,
            @Schema(description = "سود روزشمار", example = "true") @NotNull @JsonProperty("dailyInterest")
                    Boolean dailyInterest) {}

    @Schema(name = "PenaltyPolicyDto", description = "سیاست جریمه")
    public record PenaltyPolicyDto(
            @Schema(description = "نرخ جریمه", example = "6.5") @NotBlank @JsonProperty("penaltyRate")
                    String penaltyRate,
            @Schema(description = "نرخ سود تعویق", example = "3.0") @NotBlank @JsonProperty("deferralInterestRate")
                    String deferralInterestRate,
            @Schema(description = "فرمول جریمه") @NotBlank @JsonProperty("formula") String formula,
            @Schema(description = "نوع پرداخت جریمه") @NotNull @JsonProperty("paymentType")
                    PenaltyPaymentType paymentType) {}

    @Schema(name = "InstallmentPolicyDto", description = "سیاست اقساط")
    public record InstallmentPolicyDto(
            @Schema(description = "دوره اقساط", example = "P1M") @NotNull @JsonProperty("installmentPeriod")
                    Period installmentPeriod,
            @Schema(description = "فرمول محاسبه قسط") @NotBlank @JsonProperty("installmentFormula")
                    String installmentFormula,
            @Schema(description = "فرمول جزء سود قسط") @NotBlank @JsonProperty("interestComponentFormula")
                    String interestComponentFormula,
            @Schema(description = "نوع پرداخت اقساط") @NotNull @JsonProperty("paymentType")
                    InstallmentPaymentType paymentType,
            @Schema(description = "تعریف خودکار اقساط؟", example = "true")
                    @NotNull
                    @JsonProperty("isDefineAutomaticInstallment")
                    Boolean isDefineAutomaticInstallment) {}

    @Schema(name = "GracePeriodPolicyDto", description = "سیاست دوره مهلت")
    public record GracePeriodPolicyDto(
            @Schema(description = "حداقل روز مهلت", example = "10") @NotNull @JsonProperty("minGracePeriodDays")
                    Integer minGracePeriodDays,
            @Schema(description = "حداکثر روز مهلت", example = "30") @NotNull @JsonProperty("maxGracePeriodDays")
                    Integer maxGracePeriodDays,
            @Schema(description = "فرمول محاسبه مهلت") @NotBlank @JsonProperty("formula") String formula) {}

    @Schema(name = "RepaymentPriorityPolicyDto", description = "سیاست اولویت بازپرداخت")
    public record RepaymentPriorityPolicyDto(
            @NotNull @JsonProperty("principalPriority") Integer principalPriority,
            @NotNull @JsonProperty("interestPriority") Integer interestPriority,
            @NotNull @JsonProperty("penaltyPriority") Integer penaltyPriority,
            @NotNull @JsonProperty("commissionPriority") Integer commissionPriority,
            @NotNull @JsonProperty("insurancePriority") Integer insurancePriority,
            @NotNull @JsonProperty("insurancePenaltyPriority") Integer insurancePenaltyPriority,
            @NotNull @JsonProperty("hasEqualPriority") Boolean hasEqualPriority) {}

    @Schema(name = "RegulatoryCompliancePolicyDto", description = "سیاست الزامات قانونی")
    public record RegulatoryCompliancePolicyDto(
            @NotNull @JsonProperty("overDuePeriod") Integer overDuePeriod,
            @NotNull @JsonProperty("deferralPeriod") Integer deferralPeriod,
            @NotNull @JsonProperty("suspiciousPeriod") Integer suspiciousPeriod) {}

    @Schema(name = "CollateralPolicyDto", description = "سیاست وثایق")
    public record CollateralPolicyDto(
            @Schema(description = "درصد کل وثایق", example = "120") @NotNull @JsonProperty("totalPercent")
                    Integer totalPercent,
            @Schema(description = "انواع وثیقه") @NotNull @JsonProperty("collateralTypes")
                    Set<CollateralTypeDto> collateralTypes,
            @Schema(description = "انواع روش محاسبه وثیقه") @NotNull @JsonProperty("collateralCalculationType")
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
