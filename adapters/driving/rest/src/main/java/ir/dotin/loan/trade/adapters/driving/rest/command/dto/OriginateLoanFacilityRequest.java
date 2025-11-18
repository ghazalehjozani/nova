package ir.dotin.loan.trade.adapters.driving.rest.command.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OriginateLoanFacilityRequest", description = "درخواست ایجاد تسهیلات")
public record OriginateLoanFacilityRequest(
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
        @Schema(description = "درخواست تسهیلات", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Valid
                LoanApplicationDto loanApplication,
        @Schema(
                        description =
                                """
        برنامه اقساط - الزامی بودن بستگی به نوع پرداخت اقساط در  وام دارد:

        • GRADUAL: الزامی است - برنامه اقساط باید توسط کاربر ارسال شود
        • ONE_TIME: اختیاری - اقساط به صورت یک‌جا در سررسید پرداخت می‌شود
        • SCHEDULED: اختیاری - اقساط به صورت خودکار در مرحله اعطا بر اساس سیاست‌های آرایش وام ایجاد می‌شود
        ...
        """,
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                @Nullable
                @Valid
                InstallmentSchedulePlanDto installmentSchedulePlan) {

    @Schema(name = "LoanApplicationDto", description = "اطلاعات درخواست تسهیلات")
    public record LoanApplicationDto(
            @Schema(
                            description = "تاریخ درخواست",
                            example = "2025-01-01T00:00:00Z",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    Instant requestDate,
            @Schema(description = "شماره مشتری", example = "10088", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotBlank
                    String customerNumber,
            @Schema(description = "مبلغ درخواستی", example = "1000000.00", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    BigDecimal requestedAmount,
            @Schema(description = "نوع ارز", example = "IRR", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank
                    String currency,
            @Schema(description = "مدت زمان تسهیلات", example = "P12M", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    Period requestedLoanDuration,
            @Schema(
                            description = "کانال متقاضی",
                            example = "INTERNET_BANK",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    ApplicantChannel applicantChannel,
            @Schema(description = "دوره مهلت", example = "P1M", requiredMode = Schema.RequiredMode.REQUIRED) @Nullable
                    Period gracePeriod,
            @Schema(description = "تعداد اقساط", example = "12", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    Integer installmentCount,
            @Schema(description = "مقصد پرداخت", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    DisburseDestinationDto disburseDestination,
            @Schema(description = "کد بخش اقتصادی", example = "5-2", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    @NotBlank
                    String economicSectorCode,
            @Schema(
                            description = "کد دلیل درخواست",
                            example = "REASON-001",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    @NotBlank
                    String requestReasonCode,
            @Schema(description = "کد منبع فرعی", example = "SUB-001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @NotBlank
                    String subSourceCode,
            @Schema(
                            description = "توضیحات",
                            example = "توضیحات مربوط به درخواست",
                            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @NotBlank
                    String description,
            @Schema(
                            description = "شماره مشتریان ضامن",
                            example = "[\"10088\"]",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    Set<String> guarantorNumbers,
            @Schema(
                            description = "سریال‌های گواهی‌ها",
                            example = "[\"10\"]",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @Nullable
                    Set<String> certificateSerials,
            @Schema(description = "مقدار رتبه اعتباری", example = "A", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    @NotBlank
                    String credibilityRank) {}

    @Schema(name = "DisburseDestinationDto", description = "مقصد پرداخت")
    public record DisburseDestinationDto(
            @Schema(
                            description = "شماره حساب",
                            example = "1.10.10088.2",
                            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    String depositNumber,
            @Schema(description = "نوع مقصد پرداخت", example = "DEPOSIT", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull(message = "نوع مقصد پرداخت الزامی است.")
                    DisburseDestinationType type) {}

    @Schema(name = "InstallmentSchedulePlanDto", description = "برنامه اقساط")
    public record InstallmentSchedulePlanDto(
            @Schema(description = "اطلاعات اقساط", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty @Valid
                    List<InstallmentSpecDto> installments) {}

    @Schema(name = "InstallmentSpecDto", description = "مشخصات قسط")
    public record InstallmentSpecDto(
            @Schema(description = "شماره ترتیب قسط", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    Integer sequenceNumber,
            @Schema(
                            description = "سررسید قسط",
                            example = "2025-03-04T00:00:00Z",
                            requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    LocalDate dueDate,
            @Schema(description = "مبلغ اصل", example = "10000.00", requiredMode = Schema.RequiredMode.REQUIRED)
                    @NotNull
                    BigDecimal principalAmount,
            @Schema(description = "مبلغ سود", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                    BigDecimal interestAmount,
            @Schema(description = "مبلغ جریمه", example = "100.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    BigDecimal penaltyAmount,
            @Schema(description = "مبلغ کارمزد", example = "50.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                    @Nullable
                    BigDecimal feeAmount) {}
}
