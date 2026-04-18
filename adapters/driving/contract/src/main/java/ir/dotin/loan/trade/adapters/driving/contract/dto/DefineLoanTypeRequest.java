package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ir.dotin.platform.protocol.api.request.BaseRequest;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DefineLoanTypeRequest", description = "تعریف نوع تسهیلات")
public record DefineLoanTypeRequest(
        @Schema(description = "کد", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "عنوان", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = "محل استفاده", requiredMode = Schema.RequiredMode.REQUIRED)
        GatewayType gatewayType,

        @Schema(description = "امکان درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean loanApplicationAllowed,

        @Schema(description = "بخش‌های اقتصادی و ارز های مرتبط", requiredMode = Schema.RequiredMode.REQUIRED)
        List<EconomicSectorCurrencyDto> economicSectorCurrencies,

        @Schema(description = "کدهای شرایط تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
        Set<String> loanArrangementCodes,

        @Schema(description = "سرفصل ها", requiredMode = Schema.RequiredMode.REQUIRED)
        List<RelationTypeLoanTopicDto> relationTypeLoanTopics,

        Map<String, String> metadata)
        implements BaseRequest {

    @Schema(name = "EconomicSectorCurrencyDto", description = "بخش اقتصادی و نوع ارز مرتبط")
    public record EconomicSectorCurrencyDto(
            @Schema(description = "کد بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED)
            String economicSectorCode,

            @Schema(description = "نوع ارز", requiredMode = Schema.RequiredMode.REQUIRED)
            Set<String> currencyTypes) {}

    @Schema(name = "RelationTypeLoanTopicDto", description = "رابطه نوع تسهیلات با موضوع")
    public record RelationTypeLoanTopicDto(
            @Schema(description = "نوع رابطه", requiredMode = Schema.RequiredMode.REQUIRED)
            TradeRelationType relationType,

            @Schema(description = "نام موضوع مرتبط", requiredMode = Schema.RequiredMode.REQUIRED)
            String topicName,

            @Schema(description = "کد موضوع مرتبط", requiredMode = Schema.RequiredMode.REQUIRED)
            String topicCode,

            @Schema(description = "کدهای بخش‌های اقتصادی مرتبط", requiredMode = Schema.RequiredMode.REQUIRED)
            Set<String> economicSectorCodes) {}
}
