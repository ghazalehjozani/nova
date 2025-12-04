package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

/**
 * Compensates lump sum disbursement, reverting to ISSUE_CONTRACT. All transactions are cleared from aggregate's
 * internal list. Valid from: FULLY_DISBURSED
 */
@Builder
public record CompensateLumpSumDisbursementCommand(UUID uid, Long version, UUID loanFacilityId) implements Command {}
