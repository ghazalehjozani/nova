package ir.dotin.loan.trade.adapters.driving.web.controller.dto;

import java.math.BigDecimal;
import java.time.Period;
import java.util.List;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Complete request for establishing a new trade loan arrangement")
public record EstablishTradeLoanArrangementRequest(

        @NotBlank(message = "{field.required.code}")
                @Size(min = 3, max = 50, message = "{field.size.code}")
                @Pattern(regexp = "^[A-Z][A-Z0-9_-]*$", message = "{field.pattern.code}")
                @Schema(
                        description = "Unique arrangement code",
                        example = "TRD_LOAN_001",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @JsonProperty(required = true)
                String code,
        @NotBlank(message = "{field.required.title}")
                @Size(min = 5, max = 200, message = "{field.size.title}")
                @Schema(
                        description = "Human-readable title",
                        example = "Standard Trade Finance Loan",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @JsonProperty(required = true)
                String title,
        @NotEmpty(message = "{field.required.currency}")
                @Size(max = 10, message = "{collateral.types.max.exceeded}")
                @Schema(
                        description = "Supported currency codes",
                        example = "[\"USD\", \"EUR\", \"IRR\"]",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                Set<@Pattern(regexp = "^[A-Z]{3}$", message = "{field.pattern.currency}") String> currencies,
        @NotNull(message = "{field.required.amount}")
                @Valid
                @Schema(description = "Allowed amount range for loans", requiredMode = Schema.RequiredMode.REQUIRED)
                AmountRangeDto amountRange,
        @NotNull(message = "{field.required}")
                @Valid
                @Schema(description = "Allowed duration range for loans", requiredMode = Schema.RequiredMode.REQUIRED)
                DurationRangeDto durationRange,
        @NotNull(message = "{party.type.required}")
                @Schema(
                        description = "Customer party type",
                        example = "LEGAL",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                PartyType customerType,
        @PositiveOrZero(message = "{guarantor.count.negative}")
                @Max(value = 10, message = "{guarantor.count.max.exceeded}")
                @Schema(description = "Required number of guarantors", example = "2")
                Integer guarantorCount,
        @NotNull(message = "{field.required}")
                @Schema(description = "Method of loan disbursement", requiredMode = Schema.RequiredMode.REQUIRED)
                DisbursementMethod disbursementMethod,
        @NotNull(message = "{interest.rate.required}")
                @Valid
                @Schema(
                        description = "Interest calculation and application policies",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                InterestPolicyDto interestPolicy,
        @NotNull(message = "{penalty.rate.required}")
                @Valid
                @Schema(description = "Penalty calculation policies", requiredMode = Schema.RequiredMode.REQUIRED)
                PenaltyPolicyDto penaltyPolicy,
        @NotNull(message = "{installment.period.required}")
                @Valid
                @Schema(
                        description = "Installment structure and calculation policies",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                InstallmentPolicyDto installmentPolicy,
        @NotNull(message = "{grace.period.min.required}")
                @Valid
                @Schema(description = "Grace period policies", requiredMode = Schema.RequiredMode.REQUIRED)
                GracePeriodPolicyDto gracePeriodPolicy,
        @NotNull(message = "{repayment.priority.required}")
                @Valid
                @Schema(
                        description = "Priority order for repayment allocation",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                RepaymentPriorityPolicyDto repaymentPriorityPolicy,
        @NotNull(message = "{regulatory.overdue.required}")
                @Valid
                @Schema(description = "Regulatory compliance thresholds", requiredMode = Schema.RequiredMode.REQUIRED)
                RegulatoryCompliancePolicyDto regulatoryCompliancePolicy,
        @NotNull(message = "{collateral.types.required}")
                @Valid
                @Schema(
                        description = "Collateral requirements and policies",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                CollateralPolicyDto collateralPolicy,
        @Schema(description = "Has installment card feature", defaultValue = "false") boolean hasInstallmentCard,
        @Schema(description = "Enable auto-approval", defaultValue = "false") boolean autoApproval,
        @NotNull(message = "{field.required}")
                @Schema(
                        description = "Life insurance payment configuration",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                LifeInsurancePaymentType lifeInsurancePaymentType,
        @NotNull(message = "{field.required}")
                @Schema(description = "Secondary loan classification", requiredMode = Schema.RequiredMode.REQUIRED)
                LoanSecondaryType loanSecondaryType,
        @NotNull(message = "{field.required}")
                @Schema(description = "Business section classification", requiredMode = Schema.RequiredMode.REQUIRED)
                SectionType sectionType) {

    @Schema(description = "Interest policy configuration")
    public record InterestPolicyDto(
            @NotNull(message = "{interest.rate.required}")
                    @DecimalMin(value = "0", message = "{interest.rate.negative}")
                    @DecimalMax(value = "100", message = "{interest.rate.max.exceeded}")
                    @Digits(integer = 3, fraction = 6, message = "{interest.rate.format.invalid}")
                    @Schema(
                            description = "Minimum interest rate percentage",
                            example = "12.000000",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    BigDecimal minRate,
            @NotNull(message = "{interest.rate.required}")
                    @DecimalMin(value = "0", message = "{interest.rate.negative}")
                    @DecimalMax(value = "100", message = "{interest.rate.max.exceeded}")
                    @Digits(integer = 3, fraction = 6, message = "{interest.rate.format.invalid}")
                    @Schema(
                            description = "Maximum interest rate percentage",
                            example = "25.000000",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    BigDecimal maxRate,
            @NotBlank(message = "{field.required}")
                    @Size(max = 1000, message = "{field.size.max.exceeded}")
                    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
                    @Schema(
                            description = "Interest calculation formula",
                            example = "P * R * T / 365",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    String interestFormula,
            @NotBlank(message = "{field.required}")
                    @Size(max = 1000, message = "{field.size.max.exceeded}")
                    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
                    @Schema(
                            description = "Refund interest calculation formula",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    String refundFormula,
            @NotNull(message = "{field.required}")
                    @Schema(description = "Apply daily interest calculation", defaultValue = "false")
                    Boolean dailyInterest) {}

    @Schema(description = "Penalty policy configuration")
    public record PenaltyPolicyDto(
            @NotNull(message = "{penalty.rate.required}")
                    @DecimalMin(value = "0", message = "{field.number.negative.not.allowed}")
                    @DecimalMax(value = "100", message = "{penalty.rate.invalid}")
                    @Digits(integer = 3, fraction = 6, message = "{field.decimal.format}")
                    @Schema(
                            description = "Annual penalty rate percentage",
                            example = "24.000000",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    BigDecimal penaltyRate,
            @NotNull(message = "{field.required}")
                    @DecimalMin(value = "0", message = "{field.number.negative.not.allowed}")
                    @DecimalMax(value = "100", message = "{field.number.max}")
                    @Digits(integer = 3, fraction = 6, message = "{field.decimal.format}")
                    @Schema(
                            description = "Deferral interest rate percentage",
                            example = "20.000000",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    BigDecimal deferralInterestRate,
            @NotNull(message = "{penalty.type.required}")
                    @Schema(description = "How penalties are applied", requiredMode = Schema.RequiredMode.REQUIRED)
                    PenaltyPaymentType paymentType,
            @NotBlank(message = "{penalty.formula.required}")
                    @Size(max = 1000, message = "{penalty.formula.too.long}")
                    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
                    @Schema(description = "Penalty calculation formula", requiredMode = Schema.RequiredMode.REQUIRED)
                    String formula) {}

    @Schema(description = "Installment policy configuration")
    public record InstallmentPolicyDto(
            @NotNull(message = "{field.required}")
                    @Min(value = 1, message = "{field.number.min}")
                    @Max(value = 360, message = "{field.number.max}")
                    @Schema(
                            description = "Minimum number of installments",
                            example = "1",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer minCount,
            @NotNull(message = "{field.required}")
                    @Min(value = 1, message = "{field.number.min}")
                    @Max(value = 360, message = "{field.number.max}")
                    @Schema(
                            description = "Maximum number of installments",
                            example = "120",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer maxCount,
            @NotNull(message = "{installment.period.required}")
                    @Schema(
                            description = "Installment period",
                            example = "P1M",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Period installmentPeriod,
            @NotBlank(message = "{installment.formula.required}")
                    @Size(max = 1000, message = "{field.size.max.exceeded}")
                    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
                    @Schema(
                            description = "Installment calculation formula",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    String installmentFormula,
            @NotBlank(message = "{field.required}")
                    @Size(max = 1000, message = "{field.size.max.exceeded}")
                    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
                    @Schema(description = "Interest component formula", requiredMode = Schema.RequiredMode.REQUIRED)
                    String interestComponentFormula,
            @NotNull(message = "{installment.payment.type.required}")
                    @Schema(description = "Installment payment type", requiredMode = Schema.RequiredMode.REQUIRED)
                    InstallmentPaymentType paymentType,
            @NotNull(message = "{field.required}")
                    @Schema(description = "Define automatic installment schedule", defaultValue = "true")
                    Boolean isDefineAutomaticInstallment) {}

    @Schema(description = "Grace period policy configuration")
    public record GracePeriodPolicyDto(
            @NotNull(message = "{grace.period.min.required}")
                    @Min(value = 0, message = "{field.number.negative.not.allowed}")
                    @Max(value = 365, message = "{field.number.max}")
                    @Schema(
                            description = "Minimum grace period in days",
                            example = "0",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer minGracePeriodDays,
            @NotNull(message = "{grace.period.max.required}")
                    @Min(value = 0, message = "{field.number.negative.not.allowed}")
                    @Max(value = 730, message = "{field.number.max}")
                    @Schema(
                            description = "Maximum grace period in days",
                            example = "90",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer maxGracePeriodDays,
            @NotBlank(message = "{grace.period.formula.required}")
                    @Size(max = 1000, message = "{field.size.max.exceeded}")
                    @Pattern(regexp = "^[A-Za-z0-9+\\-*/()\\s.]+$", message = "{field.pattern.invalid}")
                    @Schema(
                            description = "Grace period calculation formula",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    String formula) {}

    @Schema(description = "Repayment priority policy configuration")
    public record RepaymentPriorityPolicyDto(
            @NotNull(message = "{repayment.priority.required}")
                    @Min(value = 0, message = "{repayment.priority.invalid}")
                    @Max(value = 6, message = "{repayment.priority.invalid}")
                    @Schema(
                            description = "Principal amount priority (0=highest)",
                            example = "1",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer principalPriority,
            @NotNull(message = "{repayment.priority.required}")
                    @Min(value = 0, message = "{repayment.priority.invalid}")
                    @Max(value = 6, message = "{repayment.priority.invalid}")
                    @Schema(
                            description = "Interest amount priority",
                            example = "2",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer interestPriority,
            @NotNull(message = "{repayment.priority.required}")
                    @Min(value = 0, message = "{repayment.priority.invalid}")
                    @Max(value = 6, message = "{repayment.priority.invalid}")
                    @Schema(
                            description = "Penalty amount priority",
                            example = "0",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer penaltyPriority,
            @NotNull(message = "{repayment.priority.required}")
                    @Min(value = 0, message = "{repayment.priority.invalid}")
                    @Max(value = 6, message = "{repayment.priority.invalid}")
                    @Schema(
                            description = "Commission amount priority",
                            example = "3",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer commissionPriority,
            @NotNull(message = "{repayment.priority.required}")
                    @Min(value = 0, message = "{repayment.priority.invalid}")
                    @Max(value = 6, message = "{repayment.priority.invalid}")
                    @Schema(
                            description = "Insurance amount priority",
                            example = "4",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer insurancePriority,
            @NotNull(message = "{repayment.priority.required}")
                    @Min(value = 0, message = "{repayment.priority.invalid}")
                    @Max(value = 6, message = "{repayment.priority.invalid}")
                    @Schema(
                            description = "Insurance penalty priority",
                            example = "5",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer insurancePenaltyPriority,
            @NotNull(message = "{field.required}")
                    @Schema(description = "All priorities are equal", defaultValue = "false")
                    Boolean hasEqualPriority) {}

    @Schema(description = "Regulatory compliance policy configuration")
    public record RegulatoryCompliancePolicyDto(
            @NotNull(message = "{regulatory.required}")
                    @Schema(
                            description = "Requires regulatory compliance",
                            example = "true",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Boolean requiresRegulatory,
            @Size(max = 50, message = "{field.size.max.exceeded}")
                    @Schema(description = "Regulator identifier", example = "CBI_001")
                    String regulatorId,
            @Size(max = 50, message = "{field.size.max.exceeded}")
                    @Schema(description = "Compliance code", example = "TRADE_COMPLIANCE_2024")
                    String complianceCode,
            @NotNull(message = "{regulatory.overdue.required}")
                    @Min(value = 1, message = "{regulatory.overdue.negative}")
                    @Max(value = 365, message = "{field.number.max}")
                    @Schema(
                            description = "Overdue period in days",
                            example = "30",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer overDuePeriod,
            @NotNull(message = "{regulatory.deferral.required}")
                    @Min(value = 1, message = "{field.number.negative.not.allowed}")
                    @Max(value = 365, message = "{field.number.max}")
                    @Schema(
                            description = "Deferral period in days",
                            example = "60",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer deferralPeriod,
            @NotNull(message = "{regulatory.suspicious.required}")
                    @Min(value = 1, message = "{field.number.negative.not.allowed}")
                    @Max(value = 730, message = "{field.number.max}")
                    @Schema(
                            description = "Suspicious period in days",
                            example = "90",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer suspiciousPeriod) {}

    @Schema(description = "Collateral policy configuration")
    public record CollateralPolicyDto(
            @NotEmpty(message = "{collateral.types.required}")
                    @Size(max = 20, message = "{collateral.types.max.exceeded}")
                    @Valid
                    @Schema(description = "Accepted collateral types", requiredMode = Schema.RequiredMode.REQUIRED)
                    List<CollateralTypeDto> collateralTypes,
            @NotNull(message = "{field.required}")
                    @Min(value = 100, message = "{collateral.percent.min}")
                    @Max(value = 500, message = "{collateral.percent.max}")
                    @Schema(
                            description = "Total collateral coverage percentage",
                            example = "150",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer totalPercent) {}

    @Schema(description = "Collateral type specification")
    public record CollateralTypeDto(
            @NotBlank(message = "{field.required.code}")
                    @Size(max = 20, message = "{field.size.max.exceeded}")
                    @Schema(
                            description = "Collateral type code",
                            example = "REAL_ESTATE",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    String code,
            @NotBlank(message = "{collateral.name.required}")
                    @Size(min = 3, max = 100, message = "{field.size.invalid}")
                    @Schema(
                            description = "Collateral type name",
                            example = "Real Estate Property",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    String name) {}

    @Schema(description = "Amount range specification with currency")
    public record AmountRangeDto(
            @NotNull(message = "{field.required.amount}")
                    @Positive(message = "{field.number.positive}")
                    @DecimalMin(value = "1000", message = "{amount.min.too.low}")
                    @Digits(integer = 12, fraction = 2, message = "{field.decimal.format}")
                    @Schema(
                            description = "Minimum loan amount",
                            example = "1000000.00",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    BigDecimal minAmount,
            @NotNull(message = "{field.required.amount}")
                    @Positive(message = "{field.number.positive}")
                    @DecimalMax(value = "999999999999", message = "{amount.max.exceeded}")
                    @Digits(integer = 12, fraction = 2, message = "{field.decimal.format}")
                    @Schema(
                            description = "Maximum loan amount",
                            example = "100000000.00",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    BigDecimal maxAmount) {

        @AssertTrue(message = "{amount.range.invalid}")
        @Schema(hidden = true)
        public boolean isRangeValid() {
            return minAmount == null || maxAmount == null || maxAmount.compareTo(minAmount) > 0;
        }
    }

    @Schema(description = "Duration range specification in days")
    public record DurationRangeDto(
            @NotNull(message = "{field.required}")
                    @Min(value = 1, message = "{duration.min.days}")
                    @Max(value = 7300, message = "{duration.max.days}")
                    @Schema(
                            description = "Minimum loan duration in days",
                            example = "30",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer minDurationDays,
            @NotNull(message = "{field.required}")
                    @Min(value = 1, message = "{duration.min.days}")
                    @Max(value = 7300, message = "{duration.max.years.exceeded}")
                    @Schema(
                            description = "Maximum loan duration in days",
                            example = "1825",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    Integer maxDurationDays) {

        @AssertTrue(message = "{duration.range.invalid}")
        @Schema(hidden = true)
        public boolean isDurationRangeValid() {
            return minDurationDays == null || maxDurationDays == null || maxDurationDays > minDurationDays;
        }
    }
}
