package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AddGuarantorsRequest", description = "عملیات افزودن ضامن/ضامنین تسهیلات")
public record AddGuarantorsRequest(
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        Long version,

        @Schema(description = "فهرست ضامنین برای افزودن", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty
        List<@Valid GuarantorDto> guarantors,

        Map<String, String> metadata)
        implements BaseRequest {

    @Schema(description = "اطلاعات ضامن")
    public record GuarantorDto(
            @Schema(description = "شماره مشتری ضامن", example = "67890", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String customerNumber,

            @Schema(description = "درصد ضمانت", example = "100", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
            Integer guaranteePercentage) {}
}
