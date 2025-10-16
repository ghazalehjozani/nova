package ir.dotin.loan.trade.core.application.ports.outbound.query.request;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentStatus;

public record TradeInstallmentQueryDto(
        UUID id,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String createdBy,
        String modifiedBy,
        Integer sequenceNumber,
        InstallmentAmountEmbDto scheduledAmount,
        LocalDate dueDate,
        InstallmentStatus status,
        MoneyEmbDto paidAmount,
        MoneyEmbDto outstandingAmount,
        LocalDate lastPaymentDate)
        implements Serializable {

    public record InstallmentAmountEmbDto(
            MoneyEmbDto totalAmount, MoneyEmbDto principalAmount, MoneyEmbDto interestAmount) implements Serializable {

        public record MoneyEmbDto(BigDecimal amount, String currency) implements Serializable {}
    }

    public record MoneyEmbDto(BigDecimal amount, String currency) implements Serializable {}
}
