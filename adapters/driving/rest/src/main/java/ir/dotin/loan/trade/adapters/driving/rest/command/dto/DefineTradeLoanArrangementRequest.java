package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
        @Schema(description = "کد شرط اعطا", requiredMode = Schema.RequiredMode.REQUIRED, example = "66") @NotBlank
                String code,
        @Schema(description = "عنوان", requiredMode = Schema.RequiredMode.REQUIRED, example = "شرط اعطای جدید")
                @NotBlank
                String title,
        @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED, example = "IRR") @NotBlank
                String currencyType,
        @Schema(description = "بازه مبلغی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                AmountRangeDto amountRange,
        @Schema(description = "بازه مدت زمان تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                LoanDurationRangeDto durationRange,
        @Schema(description = "نوع شخص", requiredMode = Schema.RequiredMode.REQUIRED, example = "REAL") @NotNull
                PartyType partyType,
        @Schema(description = "تأییدکننده", requiredMode = Schema.RequiredMode.REQUIRED, example = "1") @NotBlank
                String confirmType,
        @Schema(description = "تعداد ضامنین", requiredMode = Schema.RequiredMode.REQUIRED, example = "0") @NotNull
                Integer guarantorCount,
        @Schema(description = "روش پرداخت تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED, example = "LUMP_SUM")
                @NotNull
                DisbursementMethod disbursementMethod,
        @Schema(description = "اقساط کارت", requiredMode = Schema.RequiredMode.REQUIRED, example = "false") @NotNull
                Boolean hasInstallmentCard,
        @Schema(description = "روش پرداخت بیمه عمر", requiredMode = Schema.RequiredMode.REQUIRED, example = "NONE")
                @NotNull
                LifeInsurancePaymentType lifeInsurancePaymentType,
        @Schema(description = "نوع ثانویه تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED, example = "NONE")
                @NotNull
                LoanSecondaryType loanSecondaryType,
        @Schema(description = "بخش", requiredMode = Schema.RequiredMode.REQUIRED, example = "NONE") @NotNull
                SectionType sectionType,
        @Schema(description = "آیا تأیید خودکار دارد؟", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
                @NotNull
                Boolean autoApproval,
        @Schema(description = "بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED, example = "5-2") @NotBlank
                String economicSector,
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
            @Schema(description = "حداقل نرخ", example = "1") @NotBlank String minRate,
            @Schema(description = "حداکثر نرخ", example = "18") @NotBlank String maxRate,
            @Schema(description = "فرمول محاسبه سود", example = "10000") @NotBlank String interestFormula,
            @Schema(description = "فرمول بازگشت سود", example = "2000") @NotBlank String refundFormula,
            @Schema(description = "سود روزشمار", example = "true") @NotNull Boolean dailyInterest) {}

    @Schema(name = "PenaltyPolicyDto", description = "سیاست جریمه")
    public record PenaltyPolicyDto(
            @Schema(description = "نرخ جریمه", example = "2") @NotBlank String penaltyRate,
            @Schema(description = "نرخ سود تعویق", example = "6") @NotBlank String deferralInterestRate,
            @Schema(description = "فرمول جریمه", example = "50000") @NotBlank String formula,
            @Schema(description = "نوع پرداخت جریمه", example = "INSTALLMENT_PENALTY_PAYMENT") @NotNull
                    PenaltyPaymentType paymentType) {}

    @Schema(name = "InstallmentPolicyDto", description = "سیاست اقساط")
    public record InstallmentPolicyDto(
            @Schema(description = "دوره اقساط", example = "P1M") @NotNull Period installmentPeriod,
            @Schema(description = "فرمول محاسبه قسط", example = "2000") @NotBlank String installmentFormula,
            @Schema(description = "فرمول جزء سود قسط", example = "1000") @NotBlank String interestComponentFormula,
            @Schema(description = "نوع پرداخت اقساط", example = "GRADUAL") @NotNull InstallmentPaymentType paymentType,
            @Schema(description = "تعریف خودکار اقساط؟", example = "true") @NotNull
                    Boolean isDefineAutomaticInstallment) {}

    @Schema(name = "GracePeriodPolicyDto", description = "سیاست دوره مهلت")
    public record GracePeriodPolicyDto(
            @Schema(description = "حداقل روز مهلت", example = "10") @NotNull Integer minGracePeriodDays,
            @Schema(description = "حداکثر روز مهلت", example = "30") @NotNull Integer maxGracePeriodDays,
            @Schema(description = "فرمول محاسبه مهلت", example = "10000") @NotBlank String formula) {}

    @Schema(name = "RepaymentPriorityPolicyDto", description = "سیاست اولویت بازپرداخت")
    public record RepaymentPriorityPolicyDto(
            @Schema(description = "اولویت اصل وام", example = "1") @NotNull Integer principalPriority,
            @Schema(description = "اولویت سود", example = "2") @NotNull Integer interestPriority,
            @Schema(description = "اولویت جریمه", example = "3") @NotNull Integer penaltyPriority,
            @Schema(description = "اولویت کارمزد", example = "4") @NotNull Integer commissionPriority,
            @Schema(description = "اولویت بیمه", example = "5") @NotNull Integer insurancePriority,
            @Schema(description = "اولویت جریمه بیمه", example = "6") @NotNull Integer insurancePenaltyPriority,
            @Schema(description = "آیا اولویت برابر دارد؟", example = "false") @NotNull Boolean hasEqualPriority) {}

    @Schema(name = "RegulatoryCompliancePolicyDto", description = "سیاست الزامات قانونی")
    public record RegulatoryCompliancePolicyDto(
            @Schema(description = "دوره سررسید", example = "365") @NotNull Integer overDuePeriod,
            @Schema(description = "دوره تعویق", example = "30") @NotNull Integer deferralPeriod,
            @Schema(description = "دوره مشکوک", example = "90") @NotNull Integer suspiciousPeriod) {}

    @Schema(name = "CollateralPolicyDto", description = "سیاست وثایق")
    public record CollateralPolicyDto(
            @Schema(description = "درصد کل وثایق", example = "120") @NotNull Integer totalPercent,
            @Schema(description = "انواع وثیقه", example = "[\"ESTATE_1\", \"CHEQUE_1\"]") @NotNull
                    Set<String> collateralTypes,
            @Schema(description = "انواع روش محاسبه وثیقه", example = "BASED_ON_PRINCIPAL") @NotNull
                    CollateralCalculationType collateralCalculationType) {}

    @Schema(name = "AmountRangeDto", description = "بازه مبلغی")
    public record AmountRangeDto(
            @Schema(description = "حداقل مبلغ", example = "1000") @NotNull BigDecimal min,
            @Schema(description = "حداکثر مبلغ", example = "10000000") @NotNull BigDecimal max) {}

    @Schema(name = "LoanDurationRangeDto", description = "بازه مدت زمان تسهیلات")
    public record LoanDurationRangeDto(
            @Schema(description = "حداقل مدت", example = "P1M") @NotNull Period min,
            @Schema(description = "حداکثر مدت", example = "P24M") @NotNull Period max) {}
}
