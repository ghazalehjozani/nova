package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Component;
import net.time4j.PlainDate;
import net.time4j.calendar.PersianCalendar;

import ir.dotin.loan.trade.adapters.driving.contract.dto.CancelFacilityRequest;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;

@Component
public class CancelFacilityRequestToCommandMapper {

    public CancelFacilityCommand toCommand(CancelFacilityRequest message) {
        return new CancelFacilityCommand(
                UUID.randomUUID(),
                0L,
                null,
                message.fileNumber(),
                parseDate(message.revokeDate()),
                message.revokeDescription(),
                message.revokeReason(),
                message.revokeTransactionNumber());
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
