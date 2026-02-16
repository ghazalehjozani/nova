package ir.dotin.loan.trade.adapters.driving.messaging.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentPaymentMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.InstallmentPaymentMessage.PaymentDetailDto;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand.InstallmentPaymentItem;

/**
 * Anti-corruption layer: translates the old system's installment payment message into the Nova domain command.
 *
 * <p>Key translations:
 *
 * <ul>
 *   <li>{@code fileNumber} (legacy term) -> {@code applicationNumber} (domain term)
 *   <li>{@code transactionNumber} (legacy) -> {@code transactionReference} (domain)
 *   <li>{@code PaymentDetailDto.totalAmount} -> {@code InstallmentPaymentItem.totalPaidAmount}
 * </ul>
 */
@Component
public class InstallmentCollectionMessageMapper {

    public CollectInstallmentCommand toCommand(InstallmentPaymentMessage message) {
        List<InstallmentPaymentItem> payments = message.payments() != null
                ? message.payments().stream().map(this::toPaymentItem).toList()
                : List.of();

        return new CollectInstallmentCommand(
                UUID.randomUUID(),
                null,
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
                parseDate(dto.valueDate()),
                parseDate(dto.paymentDate()),
                dto.fullySettled());
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(date);
    }
}
