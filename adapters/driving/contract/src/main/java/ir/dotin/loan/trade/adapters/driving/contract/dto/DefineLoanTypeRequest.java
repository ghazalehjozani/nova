package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.protocol.api.request.BaseRequest;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DefineLoanTypeRequest", description = "تعریف نوع تسهیلات")
public record DefineLoanTypeRequest(
        @Schema(description = "کد", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String code,

        @Schema(description = "عنوان", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
        String title,

        @Schema(description = "محل استفاده", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
        GatewayType gatewayType,

        @Schema(description = "امکان درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean loanApplicationAllowed,

        @Schema(description = "بخش‌های اقتصادی و ارز های مرتبط", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty
        List<@Valid EconomicSectorCurrencyDto> economicSectorCurrencies,

        @Schema(description = "کدهای شرایط تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty
        Set<String> loanArrangementCodes,

        @Schema(description = "سرفصل ها", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty
        List<@Valid RelationTypeLoanTopicDto> relationTypeLoanTopics,

        Map<String, String> metadata)
        implements BaseRequest {

    @Schema(name = "EconomicSectorCurrencyDto", description = "بخش اقتصادی و نوع ارز مرتبط")
    public record EconomicSectorCurrencyDto(
            @Schema(description = "کد بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
            String economicSectorCode,

            @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty
            Set<String> currencyTypes) {}

    @Schema(name = "RelationTypeLoanTopicDto", description = "رابطه نوع تسهیلات با موضوع")
    public record RelationTypeLoanTopicDto(
            @Schema(description = "نوع رابطه", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
            TradeRelationType relationType,

            @Schema(description = "نام موضوع مرتبط", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
            String topicName,

            @Schema(description = "کد موضوع مرتبط", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
            String topicCode,

            @Schema(description = "کدهای بخش‌های اقتصادی مرتبط", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty
            Set<String> economicSectorCodes) {}
}
