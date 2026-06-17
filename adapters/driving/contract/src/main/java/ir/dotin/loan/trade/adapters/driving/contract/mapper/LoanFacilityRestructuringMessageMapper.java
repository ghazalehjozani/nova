package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

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
        // version: no optimistic-lock check for message-driven restructuring; 0L = unversioned sentinel.
        return new LoanFacilityRestructuringCommand(
                UUID.randomUUID(),
                0L,
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
                orToday(dto.dueDate()));
    }

    // dueDate arrives as a canonical Gregorian LocalDate (yyyy-MM-dd) on the FCB→nova event wire (SAW.101 §3).
    private LocalDate orToday(@Nullable LocalDate date) {
        return date != null ? date : LocalDate.now(ZoneOffset.UTC);
    }
}
