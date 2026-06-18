package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateFormulaRequest", description = "ویرایش فرمول")
public record UpdateFormulaRequest(
        @Schema(description = "عبارت فرمول", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 2000)
        String expression,

        @Schema(description = "توضیحات")
        String description,

        @Schema(description = "اتصالات متغیرها")
        List<@Valid BindingRequest> bindings,

        @Schema(description = "اطلاعات پردازشی")
        Map<String, String> metadata)
        implements BaseRequest {

    public UpdateFormulaRequest {
        metadata = metadata != null ? metadata : Map.of();
    }
}
