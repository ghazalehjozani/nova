package ir.dotin.loan.trade.core.application.query.installmentschedule.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import ir.dotin.platform.dispatcher.api.query.QueryResult;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;

public record TradeInstallmentScheduleQueryDto(
        UUID id,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String createdBy,
        String modifiedBy,
        List<TradeInstallmentQueryDto> installments,
        UUID loanFacilityId,
        MoneyEmbDto totalLoanAmount,
        CurrencyTypeEmbDto currency,
        InstallmentScheduleType scheduleType,
        InstallmentScheduleStatus status,
        Instant initiatedAt,
        Instant lastModifiedAt,
        GracePeriodEmbDto gracePeriod,
        BigDecimal interestRate)
        implements QueryResult {

    public record MoneyEmbDto(BigDecimal amount, String currency) implements Serializable {}

    public record CurrencyTypeEmbDto(String value) implements Serializable {}

    public record GracePeriodEmbDto(Integer days, Integer months, Integer years) implements Serializable {}
}
