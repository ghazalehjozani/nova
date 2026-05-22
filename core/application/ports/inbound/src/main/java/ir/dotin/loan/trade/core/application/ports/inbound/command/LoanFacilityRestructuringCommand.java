package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import ir.dotin.platform.pangaea.dispatcher.api.command.Command;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;

import lombok.Builder;

public record LoanFacilityRestructuringCommand(
        UUID uid,
        Long version,
        String applicationNumber,
        String userId,
        @NotBlank String transactionReference,
        @NotNull Integer duration,
        @Nullable @Valid InstallmentSchedulePlanDto installmentSchedulePlan)
        implements Command {

    @Builder(toBuilder = true)
    public record InstallmentSchedulePlanDto(@NotNull @Valid List<@Valid InstallmentDetailsItem> installments) {}

    public record InstallmentDetailsItem(
            int sequenceNumber, AmountDto principalAmount, AmountDto interestAmount, LocalDate dueDate) {}
}
