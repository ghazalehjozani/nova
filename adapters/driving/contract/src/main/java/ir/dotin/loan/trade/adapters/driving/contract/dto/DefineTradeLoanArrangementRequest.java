package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.CollateralCalculationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.validation.Money;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DefineTradeLoanArrangementRequest", description = "ایجاد شرایط تسهیلات")
public record DefineTradeLoanArrangementRequest(
        @Schema(description = "کد", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String code,

        @Schema(description = "عنوان", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String title,

        @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String currencyType,

        @Schema(description = "بازه مبلغی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        AmountRangeDto amountRange,

        @Schema(description = "بازه مدت زمان تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        LoanDurationRangeDto durationRange,

        @Schema(description = "نوع شخص", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        PartyType partyType,

        @Schema(description = "لیست مرجع تصویب", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty
        List<String> confirmTypes,

        @Schema(description = "تعداد ضامنین", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Integer guarantorCount,

        @Schema(description = "اقساط کارت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Boolean hasInstallmentCard,

        @Schema(description = "روش پرداخت بیمه عمر", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        LifeInsurancePaymentType lifeInsurancePaymentType,

        @Schema(description = "نوع ثانویه تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        LoanSecondaryType loanSecondaryType,

        @Schema(description = "بخش", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        SectionType sectionType,

        @Schema(description = "بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String economicSector,

        @Schema(description = "سیاست نرخ سود", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        InterestPolicyDto interestPolicy,

        @Schema(description = "سیاست جریمه", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        PenaltyPolicyDto penaltyPolicy,

        @Schema(description = "سیاست اقساط", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        InstallmentPolicyDto installmentPolicy,

        @Schema(description = "سیاست دوره مهلت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        GracePeriodPolicyDto gracePeriodPolicy,

        @Schema(description = "اولویت کسر مبالغ", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        RepaymentPriorityPolicyDto repaymentPriorityPolicy,

        @Schema(description = "انتقال به مطالبات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,

        @Schema(description = "سیاست وثایق", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
        CollateralPolicyDto collateralPolicy,

        @Schema(description = "نوع پرداخت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        DisbursementType disbursementType,

        Map<String, String> metadata)
        implements BaseRequest {

    @Schema(name = "InterestPolicyDto", description = "سیاست نرخ سود")
    public record InterestPolicyDto(
            @Schema(description = "نرخ تسهیلات") BigDecimal rate,
            @Schema(description = "حداقل نرخ ترجیهی") BigDecimal minPreferentialRate,
            @Schema(description = "حداکثر نرخ ترجیهی") BigDecimal maxPreferentialRate,
            @Schema(description = "فرمول محاسبه سود") String interestFormula,
            @Schema(description = "فرمول بازگشت سود") String refundFormula,
            @Schema(description = "سود روزشمار") Boolean dailyInterest) {}

    @Schema(name = "PenaltyPolicyDto", description = "سیاست جریمه")
    public record PenaltyPolicyDto(
            @Schema(description = "نرخ جریمه") BigDecimal penaltyRate,
            @Schema(description = "نرخ سود معوق") BigDecimal deferralInterestRate,
            @Schema(description = "فرمول جریمه") String formula,
            @Schema(description = "نوع پرداخت جریمه") PenaltyPaymentType paymentType) {}

    @Schema(name = "InstallmentPolicyDto", description = "سیاست اقساط")
    public record InstallmentPolicyDto(
            @Schema(description = "فاصله میان اقساط") Integer installmentPeriodMonths,
            @Schema(description = "فرمول محاسبه قسط") String installmentFormula,
            @Schema(description = "فرمول جزء سود قسط") String interestComponentFormula,
            @Schema(description = "نوع پرداخت اقساط") InstallmentPaymentType paymentType) {}

    @Schema(name = "GracePeriodPolicyDto", description = "سیاست دوره مهلت")
    public record GracePeriodPolicyDto(
            @Schema(description = "حداقل روز مهلت") Integer minGracePeriodDays,
            @Schema(description = "حداکثر روز مهلت") Integer maxGracePeriodDays,
            @Schema(description = "فرمول محاسبه مهلت") String formula) {}

    @Schema(name = "RepaymentPriorityPolicyDto", description = "سیاست اولویت بازپرداخت")
    public record RepaymentPriorityPolicyDto(
            @Schema(description = "اولویت اصل وام") Integer principalPriority,
            @Schema(description = "اولویت سود") Integer interestPriority,
            @Schema(description = "اولویت جریمه") Integer penaltyPriority,
            @Schema(description = "اولویت کارمزد") Integer commissionPriority,
            @Schema(description = "اولویت بیمه") Integer insurancePriority,
            @Schema(description = "اولویت جریمه بیمه") Integer insurancePenaltyPriority,
            @Schema(description = "آیا اولویت برابر دارد؟") Boolean hasEqualPriority) {}

    @Schema(name = "RegulatoryCompliancePolicyDto", description = "انتقال به مطالبات")
    public record RegulatoryCompliancePolicyDto(
            @Schema(description = "دوره سررسید") Integer overDuePeriodMonths,
            @Schema(description = "دوره تعویق") Integer deferralPeriodMonths,
            @Schema(description = "دوره مشکوک") Integer suspiciousPeriodMonths) {}

    @Schema(name = "CollateralPolicyDto", description = "سیاست وثایق")
    public record CollateralPolicyDto(
            @Schema(description = "درصد وثیقه مورد نیاز") Integer totalPercent,
            @Schema(description = "انواع وثیقه") Set<String> collateralTypes,
            @Schema(description = "نحوه محاسبه درصد وثیقه") CollateralCalculationType collateralCalculationType) {}

    @Schema(name = "AmountRangeDto", description = "بازه مبلغی")
    public record AmountRangeDto(
            @Schema(description = "حداقل مبلغ") @Money BigDecimal min,
            @Schema(description = "حداکثر مبلغ") @Money BigDecimal max) {}

    @Schema(name = "LoanDurationRangeDto", description = "بازه مدت زمان تسهیلات")
    public record LoanDurationRangeDto(
            @Schema(description = "حداقل مدت") Integer minMonths,
            @Schema(description = "حداکثر مدت") Integer maxMonths) {}
}
