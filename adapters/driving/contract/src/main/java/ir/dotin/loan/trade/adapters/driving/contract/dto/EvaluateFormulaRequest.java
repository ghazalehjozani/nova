package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.validation.constraints.Digits;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.validation.NumericInputBounds;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EvaluateFormulaRequest", description = "ارزیابی فرمول")
public record EvaluateFormulaRequest(
        @Schema(description = "ارجاع به نمونه‌های ارائه‌دهنده پارامتر")
        Map<String, String> providerRefs,

        @Schema(description = "مقادیر جایگزین متغیرها")
        Map<
                        String,
                        @Digits(
                                        integer = NumericInputBounds.MAX_INTEGER_DIGITS,
                                        fraction = NumericInputBounds.MAX_FRACTION_DIGITS,
                                        message = NumericInputBounds.MESSAGE)
                        BigDecimal>
                overrides,

        @Schema(description = "اطلاعات پردازشی")
        Map<String, String> metadata)
        implements BaseRequest {

    public EvaluateFormulaRequest {
        metadata = metadata != null ? metadata : Map.of();
    }
}
