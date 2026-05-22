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
import ir.dotin.loan.trade.adapters.driving.contract.dto.CollateralUpdateMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.CollateralUpdateMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.UpdateCollateralCommand;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CollateralUpdateHandler implements InboxMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CollateralUpdateHandler.class);

    private final ObjectMapper objectMapper;
    private final CollateralUpdateMessageMapper messageMapper;
    private final CommandDispatcher dispatcher;

    @Override
    public @NonNull String supportedMessageType() {
        return FcbEventOperationType.COLLATERAL_UPDATE.getCode();
    }

    @Override
    public @NonNull HandlerResult handle(@NonNull InboundMessage message) {
        String eventUid = "unknown";
        CollateralUpdateMessage collateralUpdateMessage;

        try {
            JsonNode rootNode = objectMapper.readTree(message.payload());
            eventUid = rootNode.path("eventUid").asString(eventUid);
            collateralUpdateMessage = objectMapper.treeToValue(rootNode, CollateralUpdateMessage.class);
        } catch (Exception e) {
            LOG.error("Malformed COLLATERAL_UPDATE message [eventUid={}]", eventUid, e);
            return HandlerResult.permanent(e);
        }

        LOG.info(
                "Processing COLLATERAL_UPDATE from Inbox [eventUid={}, fileNumber={}]",
                eventUid,
                collateralUpdateMessage.fileNumber());

        try {
            UpdateCollateralCommand command = messageMapper.toCommand(collateralUpdateMessage);
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
                    "Failed to process COLLATERAL_UPDATE [eventUid={}, fileNumber={}]",
                    eventUid,
                    collateralUpdateMessage.fileNumber(),
                    e);
            return HandlerResult.retryable(e);
        }
    }
}
