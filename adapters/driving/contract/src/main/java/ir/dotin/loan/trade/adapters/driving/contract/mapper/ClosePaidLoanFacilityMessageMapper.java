package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import net.time4j.PlainDate;
import net.time4j.calendar.PersianCalendar;

import ir.dotin.loan.trade.adapters.driving.contract.dto.ClosePaidLoanFacilityMessage;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityPaidOffCommand;

@Component
public class ClosePaidLoanFacilityMessageMapper {

    public CloseFacilityPaidOffCommand toCommand(ClosePaidLoanFacilityMessage message) {
        List<CloseFacilityPaidOffCommand.InstallmentPaymentItem> payments = message.payments() != null
                ? message.payments().stream().map(this::toPaymentItem).toList()
                : List.of();

        return new CloseFacilityPaidOffCommand(
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

    private CloseFacilityPaidOffCommand.InstallmentPaymentItem toPaymentItem(
            ClosePaidLoanFacilityMessage.PaymentDetailDto dto) {
        return new CloseFacilityPaidOffCommand.InstallmentPaymentItem(
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
