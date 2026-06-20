package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ValidateExpressionRequest", description = "اعتبارسنجی عبارت فرمول")
public record ValidateExpressionRequest(
        @Schema(description = "عبارت فرمول", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Size(max = 2000)
        String expression,

        @Schema(description = "اطلاعات پردازشی") Map<String, String> metadata)
        implements BaseRequest {

    public ValidateExpressionRequest {
        metadata = metadata != null ? metadata : Map.of();
    }
}
