package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import net.time4j.PlainDate;
import net.time4j.calendar.PersianCalendar;

import ir.dotin.loan.trade.adapters.driving.contract.dto.LoanFacilityRestructuringMessage;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LoanFacilityRestructuringCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;

@Component
public class LoanFacilityRestructuringMessageMapper {

    public LoanFacilityRestructuringCommand toCommand(LoanFacilityRestructuringMessage message, String userId) {
        List<LoanFacilityRestructuringCommand.InstallmentDetailsItem> installments = message.installmentList() != null
                ? message.installmentList().stream()
                        .map(this::toInstallmentItem)
                        .toList()
                : List.of();
        LoanFacilityRestructuringCommand.InstallmentSchedulePlanDto installmentSchedulePlanDto =
                LoanFacilityRestructuringCommand.InstallmentSchedulePlanDto.builder()
                        .installments(installments)
                        .build();
        return new LoanFacilityRestructuringCommand(
                UUID.randomUUID(),
                null,
                message.fileNumber(),
                userId,
                message.transactionNumber(),
                message.durationMonths(),
                installmentSchedulePlanDto);
    }

    private LoanFacilityRestructuringCommand.InstallmentDetailsItem toInstallmentItem(
            LoanFacilityRestructuringMessage.InstallmentDetailsDTO dto) {
        return new LoanFacilityRestructuringCommand.InstallmentDetailsItem(
                dto.installmentSequenceNumber(),
                AmountDto.builder().value(dto.principalAmount()).build(),
                AmountDto.builder().value(dto.interestAmount()).build(),
                parseDate(dto.dueDate()));
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
