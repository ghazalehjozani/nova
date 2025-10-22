package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OpenFacilityCaseRequest", description = "درخواست باز کردن پرونده تسهیلات")
public record OpenFacilityCaseRequest(
        @Schema(
                        description = "شناسه نوع تسهیلات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "شناسه نوع تسهیلات الزامی است.")
                @JsonProperty("loanTypeId")
                UUID loanTypeId,
        @Schema(
                        description = "شناسه عملیات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "شناسه عملیات الزامی است.")
                @JsonProperty("uid")
                UUID uid,
        @Schema(description = "نسخه عملیات", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "نسخه عملیات الزامی است.")
                @JsonProperty("version")
                Integer version,
        @Schema(
                        description = "شناسه قرارداد تسهیلات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "شناسه قرارداد تسهیلات الزامی است.")
                @JsonProperty("loanArrangementId")
                UUID loanArrangementId,
        @Schema(description = "درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "درخواست تسهیلات الزامی است.")
                @JsonProperty("loanApplication")
                LoanApplicationDto loanApplication,
        @Schema(description = "شناسه مشتری", example = "CUST-001", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "شناسه مشتری الزامی است.")
                @JsonProperty("customerId")
                String customerId) {

    @Schema(name = "LoanApplicationDto", description = "اطلاعات درخواست تسهیلات")
    public record LoanApplicationDto(
            @Schema(
                            description = "تاریخ درخواست",
                            example = "2025-01-01T00:00:00Z",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "تاریخ درخواست الزامی است.")
                    @JsonProperty("requestDate")
                    Instant requestDate,
            @Schema(description = "اطلاعات مشتری", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "اطلاعات مشتری الزامی است.")
                    @JsonProperty("customer")
                    PartyDto customer,
            @Schema(description = "مبلغ درخواستی", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "مبلغ درخواستی الزامی است.")
                    @JsonProperty("requestedAmount")
                    MoneyDto requestedAmount,
            @Schema(description = "نوع ارز", example = "IRR", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "نوع ارز الزامی است.")
                    @JsonProperty("currency")
                    CurrencyType currency,
            @Schema(description = "مدت زمان تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "مدت زمان تسهیلات الزامی است.")
                    @JsonProperty("requestedLoanDuration")
                    LoanDurationDto requestedLoanDuration,
            @Schema(description = "کانال متقاضی", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "کانال متقاضی الزامی است.")
                    @JsonProperty("applicantChannel")
                    ApplicantChannel applicantChannel,
            @Schema(description = "دوره مهلت", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "دوره مهلت الزامی است.")
                    @JsonProperty("gracePeriod")
                    GracePeriodDto gracePeriod,
            @Schema(description = "تعداد اقساط", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "تعداد اقساط الزامی است.")
                    @JsonProperty("installmentCount")
                    InstallmentCountDto installmentCount,
            @Schema(description = "مقصد پرداخت", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "مقصد پرداخت الزامی است.")
                    @JsonProperty("disburseDestination")
                    DisburseDestinationDto disburseDestination,
            @Schema(description = "بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "بخش اقتصادی الزامی است.")
                    @JsonProperty("economicSector")
                    EconomicSectorDto economicSector,
            @Schema(description = "اطلاعات شعبه", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "اطلاعات شعبه الزامی است.")
                    @JsonProperty("branch")
                    BranchDto branch,
            @Schema(description = "دلیل درخواست", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "دلیل درخواست الزامی است.")
                    @JsonProperty("requestReason")
                    RequestReasonDto requestReason,
            @Schema(description = "منبع فرعی", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @JsonProperty("subSource")
                    SubSourceDto subSource,
            @Schema(description = "توضیحات", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @JsonProperty("description")
                    DescriptionDto description,
            @Schema(description = "ضامن‌ها", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "ضامن‌ها الزامی است.")
                    @JsonProperty("guarantors")
                    Set<PartyDto> guarantors,
            @Schema(description = "گواهی‌ها", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "گواهی‌ها الزامی است.")
                    @JsonProperty("certificates")
                    Set<CertificateDto> certificates,
            @Schema(description = "شماره درخواست", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @JsonProperty("applicationNumber")
                    ApplicationNumberDto applicationNumber,
            @Schema(description = "رتبه اعتباری", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @JsonProperty("credibilityRank")
                    CredibilityRankDto credibilityRank,
            @Schema(description = "اقساط", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @JsonProperty("installmentSchedule")
                    UnequalInstallmentSchedule unequalInstallmentSchedule,
            @Schema(description = "روش پرداخت", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "روش پرداخت الزامی است.")
                    @JsonProperty("disbursementMethod")
                    DisbursementMethod disbursementMethod) {}

    @Schema(name = "PartyDto", description = "اطلاعات شخص")
    public record PartyDto(
            @Schema(description = "شماره مشتری", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "شماره مشتری الزامی است.")
                    @JsonProperty("customerNumber")
                    String customerNumber,
            @Schema(description = "نوع شخص", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "نوع شخص الزامی است.")
                    @JsonProperty("type")
                    PartyType type,
            @Schema(description = "نام شخص", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "نام شخص الزامی است.")
                    @JsonProperty("name")
                    PersonNameDto name) {}

    @Schema(name = "PersonNameDto", description = "نام شخص")
    public record PersonNameDto(
            @Schema(description = "نام", example = "علی", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "نام الزامی است.")
                    @JsonProperty("firstName")
                    String firstName,
            @Schema(description = "نام خانوادگی", example = "رضایی", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "نام خانوادگی الزامی است.")
                    @JsonProperty("lastName")
                    String lastName) {}

    @Schema(name = "BranchDto", description = "اطلاعات شعبه")
    public record BranchDto(
            @Schema(description = "کد شعبه", example = "BR-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "کد شعبه الزامی است.")
                    @JsonProperty("code")
                    String code,
            @Schema(description = "نام شعبه", example = "شعبه مرکزی", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "نام شعبه الزامی است.")
                    @JsonProperty("name")
                    String name) {}

    @Schema(name = "CertificateDto", description = "گواهی")
    public record CertificateDto(
            @Schema(description = "سریال گواهی", example = "CERT-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "سریال گواهی الزامی است.")
                    @JsonProperty("serial")
                    String serial) {}

    @Schema(name = "CredibilityRankDto", description = "رتبه اعتباری")
    public record CredibilityRankDto(
            @Schema(description = "مقدار رتبه اعتباری", example = "A", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "مقدار رتبه اعتباری الزامی است.")
                    @JsonProperty("value")
                    String value) {}

    @Schema(name = "DescriptionDto", description = "توضیحات")
    public record DescriptionDto(
            @Schema(
                            description = "مقدار توضیحات",
                            example = "توضیحات مربوط به درخواست",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "مقدار توضیحات الزامی است.")
                    @JsonProperty("value")
                    String value) {}

    @Schema(name = "DisburseDestinationDto", description = "مقصد پرداخت")
    public record DisburseDestinationDto(
            @Schema(description = "شماره حساب", example = "1234567890", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @JsonProperty("depositNumber")
                    String depositNumber,
            @Schema(description = "نوع مقصد پرداخت", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "نوع مقصد پرداخت الزامی است.")
                    @JsonProperty("type")
                    DisburseDestinationType type) {}

    @Schema(name = "RequestReasonDto", description = "دلیل درخواست")
    public record RequestReasonDto(
            @Schema(description = "کد دلیل", example = "REASON-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "کد دلیل الزامی است.")
                    @JsonProperty("code")
                    String code,
            @Schema(description = "نام دلیل", example = "خرید خانه", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "نام دلیل الزامی است.")
                    @JsonProperty("name")
                    String name) {}

    @Schema(name = "SubSourceDto", description = "منبع فرعی")
    public record SubSourceDto(
            @Schema(description = "کد منبع فرعی", example = "SUB-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "کد منبع فرعی الزامی است.")
                    @JsonProperty("code")
                    String code,
            @Schema(
                            description = "نام منبع فرعی",
                            example = "منبع فرعی اول",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "نام منبع فرعی الزامی است.")
                    @JsonProperty("name")
                    String name) {}

    @Schema(name = "ApplicationNumberDto", description = "شماره درخواست")
    public record ApplicationNumberDto(
            @Schema(description = "اطلاعات شعبه", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "اطلاعات شعبه الزامی است.")
                    @JsonProperty("branch")
                    BranchDto branch,
            @Schema(description = "کد نوع تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "کد نوع تسهیلات الزامی است.")
                    @JsonProperty("loanTypeCode")
                    LoanTypeCodeDto loanTypeCode,
            @Schema(description = "اطلاعات شخص", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "اطلاعات شخص الزامی است.")
                    @JsonProperty("party")
                    PartyDto party,
            @Schema(description = "سریال تعویق", example = "DEF-001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @JsonProperty("respiteSerial")
                    String respiteSerial,
            @Schema(
                            description = "مقدار مشتق شده",
                            example = "APP-2025-001",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "مقدار مشتق شده الزامی است.")
                    @JsonProperty("derivedValue")
                    String derivedValue) {}

    @Schema(name = "LoanTypeCodeDto", description = "کد نوع تسهیلات")
    public record LoanTypeCodeDto(
            @Schema(
                            description = "مقدار کد نوع تسهیلات",
                            example = "LTC-001",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "مقدار کد نوع تسهیلات الزامی است.")
                    @JsonProperty("value")
                    String value) {}

    @Schema(name = "MoneyDto", description = "مبلغ پول")
    public record MoneyDto(
            @Schema(description = "مقدار", example = "1000000.00", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "مقدار الزامی است.")
                    @JsonProperty("value")
                    BigDecimal value,
            @Schema(description = "نوع ارز", example = "IRR", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "نوع ارز الزامی است.")
                    @JsonProperty("currency")
                    CurrencyType currency) {}

    @Schema(name = "LoanDurationDto", description = "مدت زمان تسهیلات")
    public record LoanDurationDto(
            @Schema(description = "مقدار مدت زمان", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "مقدار مدت زمان الزامی است.")
                    @JsonProperty("value")
                    Period value) {}

    @Schema(name = "GracePeriodDto", description = "دوره مهلت")
    public record GracePeriodDto(
            @Schema(description = "مقدار دوره مهلت", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "مقدار دوره مهلت الزامی است.")
                    @JsonProperty("value")
                    Period value) {}

    @Schema(name = "InstallmentCountDto", description = "تعداد اقساط")
    public record InstallmentCountDto(
            @Schema(description = "مقدار تعداد اقساط", example = "24", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "مقدار تعداد اقساط الزامی است.")
                    @JsonProperty("value")
                    Integer value) {}

    @Schema(name = "EconomicSectorDto", description = "بخش اقتصادی")
    public record EconomicSectorDto(
            @Schema(description = "کد بخش اقتصادی", example = "SECTOR-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "کد بخش اقتصادی الزامی است.")
                    @JsonProperty("code")
                    String code,
            @Schema(description = "نام بخش اقتصادی", example = "صنعت", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank(message = "نام بخش اقتصادی الزامی است.")
                    @JsonProperty("name")
                    String name) {}

    @Schema(name = "unequalInstallmentSchedule", description = "اقساط")
    public record UnequalInstallmentSchedule(
            @Schema(description = "مبلغ قسط", example = "10000", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "مبلغ قسط الزامی است.")
                    @JsonProperty("amount")
                    MoneyDto amount,
            @Schema(description = "سود قسط", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "سود قسط الزامی است.")
                    @JsonProperty("interest")
                    MoneyDto interest,
            @Schema(description = "سررسید قسط", example = "1404/04/04", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "سررسید قسط الزامی است.")
                    @JsonProperty("dueDate")
                    LocalDate dueDate) {}
}
