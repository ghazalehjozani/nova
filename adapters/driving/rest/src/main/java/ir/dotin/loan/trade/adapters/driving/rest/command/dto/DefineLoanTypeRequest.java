package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;

import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DefineLoanTypeRequest", description = "درخواست تعریف نوع تسهیلات")
public record DefineLoanTypeRequest(
        @Schema(
                        description = "شناسه یکتا",
                        example = "a1b2c3d4-e5f6-7890-abcd-1234567890ef",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                UUID uid,
        @Schema(description = "مقدار کد نوع تسهیلات", example = "001", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                Integer code,
        @Schema(description = "مقدار عنوان", example = "تسهیلات کوتاه‌مدت", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank
                String title,
        @Schema(description = "درگاه ارتباطی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                GatewayType gatewayType,
        @Schema(
                        description = "آیا درخواست تسهیلات مجاز است؟",
                        example = "true",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                boolean loanApplicationAllowed,
        @Schema(description = "نوع بخش‌بندی مشتریان", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                SegmentType segmentType,
        @Schema(description = "بخش‌های اقتصادی و نوع ارز مرتبط", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                List<EconomicSectorCurrencyDto> economicSectorCurrencies,
        @Schema(
                        description = "شناسه‌های نوع قرارداد تسهیلات",
                        example =
                                "[\"b7e4b41c-1f1f-43a2-954f-d1a0a912f789\", \"a1b2c3d4-e5f6-7890-abcd-1234567890ef\"]",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                Set<UUID> loanArrangementIds,
        @Schema(
                        description = "شناسه‌های منبع درآمد",
                        example =
                                "[\"9ad13a5a-4c74-4963-93e3-8d7c3e87f812\", \"f1e2d3c4-b5a6-7890-cdef-123456789abc\"]",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @Nullable
                Set<UUID> incomeIds,
        @Schema(
                        description = "شناسه گروه نوع تسهیلات",
                        example = "d3dcb2f8-93e5-4bc9-a84a-b11b51e21c2f",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @Nullable
                UUID groupId,
        @Schema(description = "روابط نوع تسهیلات با موضوعات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                List<RelationTypeLoanTopicDto> relationTypeLoanTopics) {

    @Schema(name = "EconomicSectorCurrencyDto", description = "بخش اقتصادی و نوع ارز مرتبط")
    public record EconomicSectorCurrencyDto(
            @Schema(description = "کد بخش اقتصادی", example = "5-2", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String economicSectorCode,
            @Schema(
                            description = "نوع ارز",
                            example = "[\"IRR\", \"EUR\"]",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    Set<String> currencyTypes) {}

    @Schema(name = "RelationTypeLoanTopicDto", description = "رابطه نوع تسهیلات با موضوع")
    public record RelationTypeLoanTopicDto(
            @Schema(description = "نوع رابطه", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    TradeRelationType relationType,
            @Schema(
                            description = "نام موضوع مرتبط",
                            example = "تسهیلات صادراتی",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String topicName,
            @Schema(description = "کد موضوع مرتبط", example = "EXPORT", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String topicCode,
            @Schema(
                            description = "کدهای بخش‌های اقتصادی مرتبط",
                            example = "[\"5-2\"]",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    Set<String> economicSectorCodes) {}
}
