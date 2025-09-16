package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Confirmation requirement configuration")
public class ConfirmationRequirementDto {

    @NotBlank(message = "{field.required}")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "{field.pattern.invalid}")
    @Schema(
            description = "Confirmer role code",
            example = "BRANCH_MANAGER",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmerRole;

    @NotBlank(message = "{field.required}")
    @Size(max = 100, message = "{field.size.max.exceeded}")
    @Schema(
            description = "Confirmer role name",
            example = "Branch Manager",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmerRoleName;

    @Schema(description = "Confirmation level", example = "1")
    private Integer level = 1;
}
