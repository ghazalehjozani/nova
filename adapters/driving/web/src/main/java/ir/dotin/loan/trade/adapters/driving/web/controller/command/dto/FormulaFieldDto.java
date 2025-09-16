package ir.dotin.loan.trade.adapters.driving.web.controller.command.dto;

import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Formula field mapping")
public class FormulaFieldDto {

    @NotBlank(message = "{field.required}")
    @Schema(description = "Field name", example = "PRINCIPAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;

    @NotNull(message = "{field.required}")
    @Schema(description = "Field type", requiredMode = Schema.RequiredMode.REQUIRED)
    private FieldTypeDto fieldType;

    @Schema(description = "Field description")
    private String description;
}
