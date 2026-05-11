package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

@Builder
public record CompensateCloseFacilityPaidOffCommand(
        UUID uid, Long version, String applicationNumber, String transactionReference) implements Command {}
