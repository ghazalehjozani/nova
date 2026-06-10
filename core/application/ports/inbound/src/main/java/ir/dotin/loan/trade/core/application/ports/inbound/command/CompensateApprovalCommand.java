package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;

import ir.dotin.platform.pangaea.servicelayer.api.command.Command;

import lombok.Builder;

/** Compensates approval, clearing sanctioned loan and reverting to APPROVAL_SUBMITTED. Valid from: APPROVED */
@Builder
public record CompensateApprovalCommand(UUID uid, Long version, UUID loanFacilityId) implements Command {}
