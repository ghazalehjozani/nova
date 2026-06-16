package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentPaymentMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentPaymentMessage.PaymentDetailDto;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand.InstallmentPaymentItem;

/** Anti-corruption layer: translates the old system's installment payment message into the Nova domain command. */
@Component
public class InstallmentCollectionMessageMapper {

    public CollectInstallmentCommand toCommand(InstallmentPaymentMessage message) {
        List<InstallmentPaymentItem> payments = message.payments() != null
                ? message.payments().stream().map(this::toPaymentItem).toList()
                : List.of();

        // version: no optimistic-lock check for message-driven collection; 0L = unversioned sentinel.
        return new CollectInstallmentCommand(
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

    private InstallmentPaymentItem toPaymentItem(PaymentDetailDto dto) {
        return new InstallmentPaymentItem(
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
        return date != null ? date : LocalDate.now();
    }
}
