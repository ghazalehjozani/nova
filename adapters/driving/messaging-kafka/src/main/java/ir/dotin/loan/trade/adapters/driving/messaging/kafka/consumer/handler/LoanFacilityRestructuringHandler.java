package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.dispatcher.api.execution.ExecutionResult;
import ir.dotin.platform.pangaea.inbox.api.HandlerResult;
import ir.dotin.platform.pangaea.inbox.api.InboxMessageHandler;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.LoanFacilityRestructuringMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.LoanFacilityRestructuringMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LoanFacilityRestructuringCommand;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
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
        String eventUid = "unknown";
        LoanFacilityRestructuringMessage restructuringMessage;

        try {
            JsonNode rootNode = objectMapper.readTree(message.payload());
            eventUid = rootNode.path("eventUid").asString(eventUid);
            restructuringMessage = objectMapper.treeToValue(rootNode, LoanFacilityRestructuringMessage.class);
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
                case ExecutionResult.BusinessFailure<?> failure ->
                    HandlerResult.permanent(
                            new IllegalStateException(failure.notification().toString()));
            };

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
