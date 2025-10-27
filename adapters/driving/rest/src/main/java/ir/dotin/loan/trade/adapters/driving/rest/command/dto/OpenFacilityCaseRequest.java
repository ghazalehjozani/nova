package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OpenFacilityCaseRequest", description = "درخواست باز کردن پرونده تسهیلات")
public record OpenFacilityCaseRequest(
        @Schema(
                        description = "شناسه نوع تسهیلات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                UUID loanTypeId,
        @Schema(
                        description = "شناسه عملیات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                UUID uid,
        @Schema(
                        description = "شناسه قرارداد تسهیلات",
                        example = "b8f6a9b2-02af-43c3-8a9d-97d4d99e6f58",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                UUID loanArrangementId,
        @Schema(description = "درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                LoanApplicationDto loanApplication,
        @Schema(description = "اقساط", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable
                PlanGradualInstallmentScheduleDTO installmentSchedule) {

    @Schema(name = "LoanApplicationDto", description = "اطلاعات درخواست تسهیلات")
    public record LoanApplicationDto(
            @Schema(
                            description = "تاریخ درخواست",
                            example = "2025-01-01T00:00:00Z",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    Instant requestDate,
            @Schema(description = "اطلاعات مشتری", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    PartyDto customer,
            @Schema(description = "مبلغ درخواستی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    MoneyDto requestedAmount,
            @Schema(description = "نوع ارز", example = "IRR", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    CurrencyTypeDto currency,
            @Schema(description = "مدت زمان تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    LoanDurationDto requestedLoanDuration,
            @Schema(description = "کانال متقاضی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    ApplicantChannel applicantChannel,
            @Schema(description = "دوره مهلت", requiredMode = Schema.RequiredMode.REQUIRED) @Nullable
                    GracePeriodDto gracePeriod,
            @Schema(description = "تعداد اقساط", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    InstallmentCountDto installmentCount,
            @Schema(description = "مقصد پرداخت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    DisburseDestinationDto disburseDestination,
            @Schema(description = "بخش اقتصادی", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    EconomicSectorDto economicSector,
            @Schema(description = "دلیل درخواست", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    RequestReasonDto requestReason,
            @Schema(description = "منبع فرعی", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable
                    SubSourceDto subSource,
            @Schema(description = "توضیحات", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable
                    DescriptionDto description,
            @Schema(description = "ضامن‌ها", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    Set<PartyDto> guarantors,
            @Schema(description = "گواهی‌ها", requiredMode = Schema.RequiredMode.REQUIRED) @Nullable
                    Set<CertificateDto> certificates,
            @Schema(description = "رتبه اعتباری", requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable
                    CredibilityRankDto credibilityRank,
            @Schema(description = "روش پرداخت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    DisbursementMethod disbursementMethod) {}

    @Schema(name = "PartyDto", description = "اطلاعات شخص")
    public record PartyDto(
            @Schema(description = "شماره مشتری", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String customerNumber) {}

    @Schema(name = "CertificateDto", description = "گواهی")
    public record CertificateDto(
            @Schema(description = "سریال گواهی", example = "CERT-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String serial) {}

    @Schema(name = "CredibilityRankDto", description = "رتبه اعتباری")
    public record CredibilityRankDto(
            @Schema(description = "مقدار رتبه اعتباری", example = "A", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String value) {}

    @Schema(name = "DescriptionDto", description = "توضیحات")
    public record DescriptionDto(
            @Schema(
                            description = "مقدار توضیحات",
                            example = "توضیحات مربوط به درخواست",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String value) {}

    @Schema(name = "DisburseDestinationDto", description = "مقصد پرداخت")
    public record DisburseDestinationDto(
            @Schema(description = "شماره حساب", example = "1234567890", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    String depositNumber,
            @Schema(description = "نوع مقصد پرداخت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    DisburseDestinationType type) {}

    @Schema(name = "RequestReasonDto", description = "دلیل درخواست")
    public record RequestReasonDto(
            @Schema(description = "کد دلیل", example = "REASON-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String code) {}

    @Schema(name = "SubSourceDto", description = "منبع فرعی")
    public record SubSourceDto(
            @Schema(description = "کد منبع فرعی", example = "SUB-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String code) {}

    @Schema(name = "MoneyDto", description = "مبلغ پول")
    public record MoneyDto(
            @Schema(description = "مقدار", example = "1000000.00", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    BigDecimal value) {}

    @Schema(name = "LoanDurationDto", description = "مدت زمان تسهیلات")
    public record LoanDurationDto(
            @Schema(description = "مقدار مدت زمان", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    Period value) {}

    @Schema(name = "GracePeriodDto", description = "دوره مهلت")
    public record GracePeriodDto(
            @Schema(description = "مقدار دوره مهلت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    Period value) {}

    @Schema(name = "InstallmentCountDto", description = "تعداد اقساط")
    public record InstallmentCountDto(
            @Schema(description = "مقدار تعداد اقساط", example = "24", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    Integer value) {}

    @Schema(name = "EconomicSectorDto", description = "بخش اقتصادی")
    public record EconomicSectorDto(
            @Schema(description = "کد بخش اقتصادی", example = "SECTOR-001", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String code) {}

    @Schema(name = "InstallmentSpecDto", description = "اقساط")
    public record InstallmentSpecDto(
            @Schema(description = "مبلغ قسط", example = "10000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    MoneyDto principalAmount,
            @Schema(description = "سود قسط", example = "1000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    MoneyDto interestAmount,
            @Schema(description = "سررسید قسط", example = "1404/04/04", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    LocalDate dueDate) {}

    @Schema(name = "PlanGradualInstallmentScheduleDTO", description = "جزئیات اقساط")
    public record PlanGradualInstallmentScheduleDTO(
            @Schema(description = "مبلغ پرونده", example = "10000", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    MoneyDto totalLoanAmount,
            @Schema(description = "نرخ سود", example = "2", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    BigDecimal interestRate,
            @Schema(description = "دوره تنفس", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) @Nullable
                    Integer gracePeriodDays,
            @Schema(description = "اطلاعات اقساط", requiredMode = Schema.RequiredMode.REQUIRED) @Nullable
                    List<InstallmentSpecDto> installments) {}

    @Schema(name = "CurrencyTypeDto", description = "نوع ارز")
    public record CurrencyTypeDto(@NotBlank String value) {}
}
