package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driving.contract.dto.CancelFacilityMessage;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;

@Component
public class CancelFacilityMessageMapper {

    public CancelFacilityCommand toCommand(CancelFacilityMessage message) {
        // loanFacilityId is null at the message boundary — the handler resolves the facility from fileNumber
        // (→ applicationNumber) via ApplicationNumberResolver. The command field is @Nullable for exactly this path.
        // revokeDate arrives as a canonical Gregorian LocalDate (yyyy-MM-dd) on the FCB→nova event wire (SAW.101 §3).
        return new CancelFacilityCommand(
                UUID.randomUUID(),
                0L,
                null,
                message.fileNumber(),
                orToday(message.revokeDate()),
                message.revokeDescription(),
                message.revokeReason(),
                message.revokeTransactionNumber());
    }

    private LocalDate orToday(@Nullable LocalDate date) {
        return date != null ? date : LocalDate.now();
    }
}
