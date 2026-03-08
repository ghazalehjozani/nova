package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.List;
import java.util.UUID;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

@Builder
public record CompensateCollectInstallmentCommand(
        UUID uid, Long version, String applicationNumber, List<String> transactionNumbers) implements Command {}
