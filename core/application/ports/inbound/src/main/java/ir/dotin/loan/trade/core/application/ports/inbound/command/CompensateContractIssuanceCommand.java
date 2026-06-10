package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

/**
 * Compensates contract issuance, reverting to APPROVED. Transaction to reverse is determined from aggregate's internal
 * list (LIFO). Valid from: ISSUE_CONTRACT
 */
@Builder
public record CompensateContractIssuanceCommand(UUID uid, Long version, UUID loanFacilityId) implements Command {}
