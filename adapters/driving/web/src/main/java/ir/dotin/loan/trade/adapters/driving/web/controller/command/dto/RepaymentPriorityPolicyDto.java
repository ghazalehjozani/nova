package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import java.util.Set;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Repayment priority policy configuration")
public class RepaymentPriorityPolicyDto {

    @NotNull(message = "{repayment.priority.required}")
    @Min(value = 0, message = "{repayment.priority.invalid}")
    @Max(value = 6, message = "{repayment.priority.invalid}")
    @Schema(
            description = "Main amount priority (0=highest)",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer installmentMainAmountPriority;

    @NotNull(message = "{repayment.priority.required}")
    @Min(value = 0, message = "{repayment.priority.invalid}")
    @Max(value = 6, message = "{repayment.priority.invalid}")
    @Schema(description = "Interest amount priority", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer installmentInterestAmountPriority;

    @NotNull(message = "{repayment.priority.required}")
    @Min(value = 0, message = "{repayment.priority.invalid}")
    @Max(value = 6, message = "{repayment.priority.invalid}")
    @Schema(description = "Penalty amount priority", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer installmentPenaltyAmountPriority;

    @NotNull(message = "{repayment.priority.required}")
    @Min(value = 0, message = "{repayment.priority.invalid}")
    @Max(value = 6, message = "{repayment.priority.invalid}")
    @Schema(description = "Income amount priority", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer installmentIncomeAmountPriority;

    @NotNull(message = "{repayment.priority.required}")
    @Min(value = 0, message = "{repayment.priority.invalid}")
    @Max(value = 6, message = "{repayment.priority.invalid}")
    @Schema(description = "Insurance amount priority", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer insuranceAmountPriority;

    @NotNull(message = "{repayment.priority.required}")
    @Min(value = 0, message = "{repayment.priority.invalid}")
    @Max(value = 6, message = "{repayment.priority.invalid}")
    @Schema(description = "Insurance penalty priority", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer insurancePenaltyAmountPriority;

    @Schema(description = "All priorities are equal", defaultValue = "false")
    private boolean hasEqualPriority = false;

    @AssertTrue(message = "{repayment.priority.duplicate}")
    @Schema(hidden = true)
    public boolean areUniquePriorities() {
        if (hasEqualPriority) return true;
        Set<Integer> priorities = Set.of(
                installmentMainAmountPriority,
                installmentInterestAmountPriority,
                installmentPenaltyAmountPriority,
                installmentIncomeAmountPriority,
                insuranceAmountPriority,
                insurancePenaltyAmountPriority);
        return priorities.size() == 6;
    }
}
