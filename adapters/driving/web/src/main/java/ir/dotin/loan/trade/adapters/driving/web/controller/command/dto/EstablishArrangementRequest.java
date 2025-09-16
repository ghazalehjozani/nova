package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import java.util.Map;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete request for establishing a new trade loan arrangement")
public class EstablishArrangementRequest {

    @NotBlank(message = "{field.required.code}")
    @Size(min = 3, max = 50, message = "{field.size.code}")
    @Pattern(regexp = "^[A-Z][A-Z0-9_-]*$", message = "{field.pattern.code}")
    @Schema(
            description = "Unique arrangement code",
            example = "TRD_LOAN_001",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(required = true)
    private String code;

    @NotBlank(message = "{field.required.title}")
    @Size(min = 5, max = 200, message = "{field.size.title}")
    @Schema(
            description = "Human-readable title",
            example = "Standard Trade Finance Loan",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(required = true)
    private String title;

    @NotEmpty(message = "{field.required.currency}")
    @Size(max = 10, message = "{collateral.types.max.exceeded}")
    @Schema(
            description = "Supported currency codes",
            example = "[\"USD\", \"EUR\", \"IRR\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<@Pattern(regexp = "^[A-Z]{3}$", message = "{field.pattern.currency}") String> currencies;

    @NotNull(message = "{field.required.amount}")
    @Valid
    @Schema(description = "Allowed amount range for loans", requiredMode = Schema.RequiredMode.REQUIRED)
    private AmountRangeDto amountRange;

    @NotNull(message = "{field.required}")
    @Valid
    @Schema(description = "Allowed duration range for loans", requiredMode = Schema.RequiredMode.REQUIRED)
    private DurationRangeDto durationRange;

    @NotNull(message = "{party.type.required}")
    @Schema(description = "Customer party type", example = "LEGAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private PartyTypeDto partyType;

    @PositiveOrZero(message = "{guarantor.count.negative}")
    @Max(value = 10, message = "{guarantor.count.max.exceeded}")
    @Schema(description = "Required number of guarantors", example = "2")
    private Integer guarantorCount = 0;

    @Valid
    @Schema(description = "Confirmation requirements")
    private ConfirmationRequirementDto confirmationRequirement;

    @NotNull(message = "{field.required}")
    @Schema(description = "Method of loan disbursement", requiredMode = Schema.RequiredMode.REQUIRED)
    private DisbursementMethodDto disbursementMethod;

    @NotNull(message = "{interest.rate.required}")
    @Valid
    @Schema(description = "Interest calculation and application policies", requiredMode = Schema.RequiredMode.REQUIRED)
    private InterestPolicyDto interestPolicy;

    @NotNull(message = "{penalty.rate.required}")
    @Valid
    @Schema(description = "Penalty calculation policies", requiredMode = Schema.RequiredMode.REQUIRED)
    private PenaltyPolicyDto penaltyPolicy;

    @NotNull(message = "{installment.period.required}")
    @Valid
    @Schema(description = "Installment structure and calculation policies", requiredMode = Schema.RequiredMode.REQUIRED)
    private InstallmentPolicyDto installmentPolicy;

    @NotNull(message = "{grace.period.min.required}")
    @Valid
    @Schema(description = "Grace period policies", requiredMode = Schema.RequiredMode.REQUIRED)
    private GracePeriodPolicyDto gracePeriodPolicy;

    @NotNull(message = "{repayment.priority.required}")
    @Valid
    @Schema(description = "Priority order for repayment allocation", requiredMode = Schema.RequiredMode.REQUIRED)
    private RepaymentPriorityPolicyDto repaymentPriorityPolicy;

    @NotNull(message = "{regulatory.overdue.required}")
    @Valid
    @Schema(description = "Regulatory compliance thresholds", requiredMode = Schema.RequiredMode.REQUIRED)
    private RegulatoryCompliancePolicyDto regulatoryCompliancePolicy;

    @NotNull(message = "{collateral.types.required}")
    @Valid
    @Schema(description = "Collateral requirements and policies", requiredMode = Schema.RequiredMode.REQUIRED)
    private CollateralPolicyDto collateralPolicy;

    @Schema(description = "Has installment card feature", defaultValue = "false")
    private boolean hasInstallmentCard = false;

    @Schema(description = "Enable auto-approval", defaultValue = "false")
    private boolean autoApproval = false;

    @NotNull(message = "{field.required}")
    @Schema(description = "Life insurance payment configuration", requiredMode = Schema.RequiredMode.REQUIRED)
    private LifeInsurancePaymentTypeDto lifeInsurancePaymentType;

    @NotNull(message = "{field.required}")
    @Schema(description = "Secondary loan classification", requiredMode = Schema.RequiredMode.REQUIRED)
    private LoanSecondaryTypeDto loanSecondaryType;

    @NotNull(message = "{field.required}")
    @Schema(description = "Business section classification", requiredMode = Schema.RequiredMode.REQUIRED)
    private SectionTypeDto sectionType;

    @Schema(description = "Additional metadata for audit trail")
    private Map<String, String> metadata;
}
