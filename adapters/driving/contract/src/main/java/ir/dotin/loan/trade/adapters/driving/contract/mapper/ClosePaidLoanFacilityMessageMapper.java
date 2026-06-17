package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.ClosePaidLoanFacilityMessage;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityPaidOffCommand;

@Component
public class ClosePaidLoanFacilityMessageMapper {

    public CloseFacilityPaidOffCommand toCommand(ClosePaidLoanFacilityMessage message) {
        List<CloseFacilityPaidOffCommand.InstallmentPaymentItem> payments = message.payments() != null
                ? message.payments().stream().map(this::toPaymentItem).toList()
                : List.of();

        // version: no optimistic-lock check for message-driven close; 0L = unversioned sentinel.
        return new CloseFacilityPaidOffCommand(
                UUID.randomUUID(),
                0L,
                message.fileNumber(),
                message.transactionNumber(),
                message.currency(),
                payments,
                message.channel(),
                message.transactionNumber(),
                null);
    }

    private CloseFacilityPaidOffCommand.InstallmentPaymentItem toPaymentItem(
            ClosePaidLoanFacilityMessage.PaymentDetailDto dto) {
        return new CloseFacilityPaidOffCommand.InstallmentPaymentItem(
                dto.installmentSequenceNumber(),
                dto.principalAmount(),
                dto.interestAmount(),
                dto.totalAmount(),
                orToday(dto.valueDate()),
                orToday(dto.paymentDate()),
                dto.fullySettled());
    }

    // Dates arrive as canonical Gregorian LocalDate (yyyy-MM-dd) on the FCB→nova event wire (SAW.101 §3).
    private LocalDate orToday(@Nullable LocalDate date) {
        return date != null ? date : LocalDate.now(ZoneOffset.UTC);
    }
}
