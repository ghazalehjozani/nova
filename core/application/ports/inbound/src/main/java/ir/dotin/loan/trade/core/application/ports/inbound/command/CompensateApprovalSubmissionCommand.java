package ir.dotin.loan.trade.core.application.ports.inbound.command;

import java.util.UUID;

import ir.dotin.platform.dispatcher.api.command.Command;

import lombok.Builder;

/** Compensates approval submission, reverting to APPLICATION_SUBMITTED. Valid from: APPROVAL_SUBMITTED */
@Builder(toBuilder = true)
public record CompensateApprovalSubmissionCommand(UUID uid, Long version, UUID loanFacilityId) implements Command {}
