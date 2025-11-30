package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import ir.dotin.loan.baseloan.core.domain.shared.enums.CollateralCalculationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DefineTradeLoanArrangementRequest", description = "ایجاد شرایط تسهیلات")
public record DefineTradeLoanArrangementRequest(
        @Schema(description = "کد", requiredMode = Schema.RequiredMode.REQUIRED, example = "66") String code,
        @Schema(description = "عنوان", requiredMode = Schema.RequiredMode.REQUIRED, example = "شرایط تسهیلات جدید")
                String title,
        @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED, example = "IRR")
                String currencyType,
        @Schema(description = "بازه مبلغی", requiredMode = Schema.RequiredMode.REQUIRED) AmountRangeDto amountRange,
        @Schema(description = "بازه مدت زمان تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                LoanDurationRangeDto durationRange,
        @Schema(description = "نوع شخص", requiredMode = Schema.RequiredMode.REQUIRED, example = "REAL")
                PartyType partyType,
        @Schema(description = "لیست مرجع تصویب", requiredMode = Schema.RequiredMode.REQUIRED) List<String> confirmTypes,
        @Schema(description = "تعداد ضامنین", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
                Integer guarantorCount,
        @Schema(description = "اقساط کارت", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
                Boolean hasInstallmentCard,
        @Schema(description = "روش پرداخت بیمه عمر", requiredMode = Schema.RequiredMode.REQUIRED, example = "NONE")
                LifeInsurancePaymentType lifeInsurancePaymentType,
        @Schema(description = "نوع ثانویه تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED, example = "NONE")
                LoanSecondaryType loanSecondaryType,
        @Schema(description = "بخش", requiredMode = Schema.RequiredMode.REQUIRED, example = "NONE")
                SectionType sectionType,
        @Schema(description = "بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED, example = "5-2")
                String economicSector,
        @Schema(description = "سیاست نرخ سود", requiredMode = Schema.RequiredMode.REQUIRED)
                InterestPolicyDto interestPolicy,
        @Schema(description = "سیاست جریمه", requiredMode = Schema.RequiredMode.REQUIRED)
                PenaltyPolicyDto penaltyPolicy,
        @Schema(description = "سیاست اقساط", requiredMode = Schema.RequiredMode.REQUIRED)
                InstallmentPolicyDto installmentPolicy,
        @Schema(description = "سیاست دوره مهلت", requiredMode = Schema.RequiredMode.REQUIRED)
                GracePeriodPolicyDto gracePeriodPolicy,
        @Schema(description = "اولویت کسر مبالغ", requiredMode = Schema.RequiredMode.REQUIRED)
                RepaymentPriorityPolicyDto repaymentPriorityPolicy,
        @Schema(description = "انتقال به مطالبات", requiredMode = Schema.RequiredMode.REQUIRED)
                RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,
        @Schema(description = "سیاست وثایق", requiredMode = Schema.RequiredMode.REQUIRED)
                CollateralPolicyDto collateralPolicy) {

    @Schema(name = "InterestPolicyDto", description = "سیاست نرخ سود")
    public record InterestPolicyDto(
            @Schema(description = "حداقل نرخ") BigDecimal minRate,
            @Schema(description = "حداکثر نرخ") BigDecimal maxRate,
            @Schema(description = "فرمول محاسبه سود", example = "10000") String interestFormula,
            @Schema(description = "فرمول بازگشت سود", example = "2000") String refundFormula,
            @Schema(description = "سود روزشمار", example = "true") Boolean dailyInterest) {}

    @Schema(name = "PenaltyPolicyDto", description = "سیاست جریمه")
    public record PenaltyPolicyDto(
            @Schema(description = "نرخ جریمه") BigDecimal penaltyRate,
            @Schema(description = "نرخ سود معوق") BigDecimal deferralInterestRate,
            @Schema(description = "فرمول جریمه", example = "50000") String formula,
            @Schema(description = "نوع پرداخت جریمه") PenaltyPaymentType paymentType) {}

    @Schema(name = "InstallmentPolicyDto", description = "سیاست اقساط")
    public record InstallmentPolicyDto(
            @Schema(description = "فاصله میان اقساط") Integer installmentPeriodMonths,
            @Schema(description = "فرمول محاسبه قسط", example = "2000") String installmentFormula,
            @Schema(description = "فرمول جزء سود قسط", example = "1000") String interestComponentFormula,
            @Schema(description = "نوع پرداخت اقساط") InstallmentPaymentType paymentType) {}

    @Schema(name = "GracePeriodPolicyDto", description = "سیاست دوره مهلت")
    public record GracePeriodPolicyDto(
            @Schema(description = "حداقل روز مهلت", example = "10") Integer minGracePeriodDays,
            @Schema(description = "حداکثر روز مهلت", example = "30") Integer maxGracePeriodDays,
            @Schema(description = "فرمول محاسبه مهلت", example = "10000") String formula) {}

    @Schema(name = "RepaymentPriorityPolicyDto", description = "سیاست اولویت بازپرداخت")
    public record RepaymentPriorityPolicyDto(
            @Schema(description = "اولویت اصل وام", example = "1") Integer principalPriority,
            @Schema(description = "اولویت سود", example = "2") Integer interestPriority,
            @Schema(description = "اولویت جریمه", example = "3") Integer penaltyPriority,
            @Schema(description = "اولویت کارمزد", example = "4") Integer commissionPriority,
            @Schema(description = "اولویت بیمه", example = "5") Integer insurancePriority,
            @Schema(description = "اولویت جریمه بیمه", example = "6") Integer insurancePenaltyPriority,
            @Schema(description = "آیا اولویت برابر دارد؟", example = "false") Boolean hasEqualPriority) {}

    @Schema(name = "RegulatoryCompliancePolicyDto", description = "انتقال به مطالبات")
    public record RegulatoryCompliancePolicyDto(
            @Schema(description = "دوره سررسید", example = "365") Integer overDuePeriodMonths,
            @Schema(description = "دوره تعویق", example = "30") Integer deferralPeriodMonths,
            @Schema(description = "دوره مشکوک", example = "90") Integer suspiciousPeriodMonths) {}

    @Schema(name = "CollateralPolicyDto", description = "سیاست وثایق")
    public record CollateralPolicyDto(
            @Schema(description = "درصد وثیقه مورد نیاز", example = "120") Integer totalPercent,
            @Schema(description = "انواع وثیقه") Set<String> collateralTypes,
            @Schema(description = "نحوه محاسبه درصد وثیقه") CollateralCalculationType collateralCalculationType) {}

    @Schema(name = "AmountRangeDto", description = "بازه مبلغی")
    public record AmountRangeDto(
            @Schema(description = "حداقل مبلغ", example = "1000") BigDecimal min,
            @Schema(description = "حداکثر مبلغ", example = "10000000") BigDecimal max) {}

    @Schema(name = "LoanDurationRangeDto", description = "بازه مدت زمان تسهیلات")
    public record LoanDurationRangeDto(
            @Schema(description = "حداقل مدت") Integer minMonths,
            @Schema(description = "حداکثر مدت") Integer maxMonths) {}
}
