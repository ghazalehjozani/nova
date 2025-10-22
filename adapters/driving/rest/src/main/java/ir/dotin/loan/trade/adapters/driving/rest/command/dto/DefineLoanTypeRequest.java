package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

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
                @NotNull(message = "شناسه یکتا الزامی است.")
                @JsonProperty("uid")
                UUID uid,
        @Schema(description = "کد نوع تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "کد نوع تسهیلات الزامی است.")
                @JsonProperty("code")
                LoanTypeCodeDto code,
        @Schema(description = "عنوان نوع تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "عنوان نوع تسهیلات الزامی است.")
                @JsonProperty("title")
                TitleDto title,
        @Schema(description = "درگاه ارتباطی", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "درگاه ارتباطی الزامی است.")
                @JsonProperty("gatewayType")
                GatewayType gatewayType,
        @Schema(description = "وضعیت مجاز بودن درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "وضعیت مجاز بودن درخواست الزامی است.")
                @JsonProperty("loanApplicationAllowed")
                LoanApplicationStatusDto loanApplicationAllowed,
        @Schema(description = "نوع بخش‌بندی مشتریان", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "نوع بخش‌بندی الزامی است.")
                @JsonProperty("segmentType")
                SegmentType segmentType,
        @Schema(description = "بخش‌های اقتصادی و نوع ارز مرتبط", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "بخش‌های اقتصادی الزامی است.")
                @JsonProperty("economicSectorCurrencies")
                Set<EconomicSectorCurrencyDto> economicSectorCurrencies,
        @Schema(description = "شناسه‌های نوع قرارداد تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "شناسه‌های نوع قرارداد تسهیلات الزامی است.")
                @JsonProperty("loanArrangementIds")
                Set<LoanArrangementIdDto> loanArrangementIds,
        @Schema(description = "شناسه‌های منبع درآمد", requiredMode = Schema.RequiredMode.REQUIRED)
                @Nullable
                @JsonProperty("incomeIds")
                Set<IncomeIdDto> incomeIds,
        @Schema(description = "شناسه گروه نوع تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @Nullable
                @JsonProperty("groupId")
                LoanTypeGroupIdDto groupId,
        @Schema(description = "روابط نوع تسهیلات با موضوعات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "روابط نوع تسهیلات الزامی است.")
                @JsonProperty("relationTypeLoanTopics")
                List<RelationTypeLoanTopicDto> relationTypeLoanTopics) {

    @Schema(name = "LoanTypeCodeDto", description = "کد نوع تسهیلات")
    public record LoanTypeCodeDto(
            @Schema(
                            description = "مقدار کد نوع تسهیلات",
                            example = "LNT-001",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "کد نوع تسهیلات الزامی است.")
                    @JsonProperty("value")
                    String value) {}

    @Schema(name = "TitleDto", description = "عنوان نوع تسهیلات")
    public record TitleDto(
            @Schema(
                            description = "مقدار عنوان",
                            example = "تسهیلات کوتاه‌مدت",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "عنوان الزامی است.")
                    @JsonProperty("value")
                    String value) {}

    @Schema(name = "LoanApplicationStatusDto", description = "وضعیت مجاز بودن درخواست تسهیلات")
    public record LoanApplicationStatusDto(
            @Schema(
                            description = "آیا درخواست تسهیلات مجاز است؟",
                            example = "true",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "وضعیت مجاز بودن الزامی است.")
                    @JsonProperty("isAllowed")
                    boolean isAllowed) {}

    @Schema(name = "EconomicSectorCurrencyDto", description = "بخش اقتصادی و نوع ارز مرتبط")
    public record EconomicSectorCurrencyDto(
            @Schema(description = "بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "بخش اقتصادی الزامی است.")
                    @JsonProperty("economicSector")
                    EconomicSectorDto economicSector,
            @Schema(description = "نوع ارز", example = "IRR", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "نوع ارز الزامی است.")
                    @JsonProperty("currencyType")
                    Set<CurrencyTypeDto> currencyTypes) {}

    @Schema(name = "EconomicSectorDto", description = "بخش اقتصادی")
    public record EconomicSectorDto(
            @Schema(description = "کد بخش اقتصادی", example = "SEC-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "کد بخش اقتصادی الزامی است.")
                    @JsonProperty("code")
                    String code) {}

    @Schema(name = "LoanArrangementIdDto", description = "شناسه نوع قرارداد تسهیلات")
    public record LoanArrangementIdDto(
            @Schema(
                            description = "شناسه قرارداد تسهیلات",
                            example = "b7e4b41c-1f1f-43a2-954f-d1a0a912f789",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "شناسه قرارداد تسهیلات الزامی است.")
                    @JsonProperty("value")
                    UUID value) {}

    @Schema(name = "IncomeIdDto", description = "شناسه منبع درآمد")
    public record IncomeIdDto(
            @Schema(
                            description = "شناسه منبع درآمد",
                            example = "9ad13a5a-4c74-4963-93e3-8d7c3e87f812",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "شناسه منبع درآمد الزامی است.")
                    @JsonProperty("value")
                    UUID value) {}

    @Schema(name = "LoanTypeGroupIdDto", description = "شناسه گروه نوع تسهیلات")
    public record LoanTypeGroupIdDto(
            @Schema(
                            description = "شناسه گروه نوع تسهیلات",
                            example = "d3dcb2f8-93e5-4bc9-a84a-b11b51e21c2f",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "شناسه گروه نوع تسهیلات الزامی است.")
                    @JsonProperty("value")
                    UUID value) {}

    @Schema(name = "RelationTypeLoanTopicDto", description = "رابطه نوع تسهیلات با موضوع")
    public record RelationTypeLoanTopicDto(
            @Schema(description = "نوع رابطه", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "نوع رابطه الزامی است.")
                    @JsonProperty("relationTypeKey")
                    TradeRelationType relationType,
            @Schema(
                            description = "نام موضوع مرتبط",
                            example = "تسهیلات صادراتی",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "نام موضوع الزامی است.")
                    @JsonProperty("topicName")
                    String topicName,
            @Schema(description = "کد موضوع مرتبط", example = "EXPORT", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "کد موضوع الزامی است.")
                    @JsonProperty("topicCode")
                    String topicCode,
            @Schema(description = "بخش اقتصادی مرتبط", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "بخش اقتصادی مرتبط الزامی است.")
                    @JsonProperty("economicSector")
                    Set<EconomicSectorDto> economicSectors) {}

    @Schema(name = "RelationTypeDto", description = "نوع رابطه")
    public record RelationTypeDto(
            @Schema(description = "کد رابطه", example = "REL-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "کد رابطه الزامی است.")
                    @JsonProperty("code")
                    String code) {}

    public record CurrencyTypeDto(@NotBlank String value) {}
}
