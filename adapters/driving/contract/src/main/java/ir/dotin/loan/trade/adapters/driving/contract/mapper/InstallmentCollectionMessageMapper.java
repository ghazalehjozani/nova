package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import net.time4j.PlainDate;
import net.time4j.calendar.PersianCalendar;

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
                parseDate(dto.valueDate()),
                parseDate(dto.paymentDate()),
                dto.fullySettled());
    }

    private LocalDate parseDate(@Nullable String date) {
        if (date == null || date.isBlank()) {
            return LocalDate.now();
        }

        String[] parts = date.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Expected yyyy/MM/dd");
        }

        PlainDate gregorian = PersianCalendar.of(
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]))
                .transform(PlainDate.class);

        return LocalDate.of(gregorian.getYear(), gregorian.getMonth(), gregorian.getDayOfMonth());
    }
}
