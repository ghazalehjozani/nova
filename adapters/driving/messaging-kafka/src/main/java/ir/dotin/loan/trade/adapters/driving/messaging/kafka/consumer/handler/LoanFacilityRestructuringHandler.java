package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.inbox.api.HandlerResult;
import ir.dotin.platform.pangaea.inbox.api.InboxMessageHandler;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.servicelayer.api.execution.ExecutionResult;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.LoanFacilityRestructuringMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.LoanFacilityRestructuringMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LoanFacilityRestructuringCommand;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class LoanFacilityRestructuringHandler implements InboxMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoanFacilityRestructuringHandler.class);

    private final ObjectMapper objectMapper;
    private final LoanFacilityRestructuringMessageMapper messageMapper;
    private final CommandDispatcher dispatcher;
    private final AuthenticationContextHolder authenticationContextHolder;

    @Override
    public @NonNull String supportedMessageType() {
        return FcbEventOperationType.LOAN_FACILITY_RESTRUCTURING.getCode();
    }

    @Override
    public @NonNull HandlerResult handle(@NonNull InboundMessage message) {
        UUID eventUid = message.headers().eventUid();
        LoanFacilityRestructuringMessage restructuringMessage;

        try {
            restructuringMessage = objectMapper.readValue(message.payload(), LoanFacilityRestructuringMessage.class);
        } catch (Exception e) {
            LOG.error("Malformed facility restructuring message [eventUid={}]", eventUid, e);
            return HandlerResult.permanent(e);
        }

        LOG.info(
                "Processing facility restructuring from Inbox [eventUid={}, fileNumber={}]",
                eventUid,
                restructuringMessage.fileNumber());

        try {
            LoanFacilityRestructuringCommand command =
                    messageMapper.toCommand(restructuringMessage, authenticationContextHolder.userIdOrThrow());
            ExecutionResult<?> executionResult = dispatcher.dispatch(command);

            return switch (executionResult) {
                case ExecutionResult.Fresh<?> ignored -> HandlerResult.success();
                case ExecutionResult.Replayed<?> ignored -> HandlerResult.success();
            };

        } catch (FailureCauseException e) {
            LOG.error(
                    "Domain rule rejected facility restructuring [eventUid={}, fileNumber={}]",
                    eventUid,
                    restructuringMessage.fileNumber(),
                    e);
            return HandlerResult.permanent(e);
        } catch (Exception e) {
            LOG.error(
                    "Failed to process facility restructuring [eventUid={}, fileNumber={}]",
                    eventUid,
                    restructuringMessage.fileNumber(),
                    e);
            return HandlerResult.retryable(e);
        }
    }
}
