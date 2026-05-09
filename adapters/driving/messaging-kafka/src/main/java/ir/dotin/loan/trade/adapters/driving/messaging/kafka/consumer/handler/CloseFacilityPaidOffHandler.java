package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.dispatcher.api.execution.ExecutionResult;
import ir.dotin.platform.inbox.api.HandlerResult;
import ir.dotin.platform.inbox.api.InboxMessageHandler;
import ir.dotin.platform.messaging.api.inbound.InboundMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.ClosePaidLoanFacilityMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.ClosePaidLoanFacilityMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityPaidOffCommand;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CloseFacilityPaidOffHandler implements InboxMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CloseFacilityPaidOffHandler.class);

    private final ObjectMapper objectMapper;
    private final ClosePaidLoanFacilityMessageMapper messageMapper;
    private final CommandDispatcher dispatcher;

    @Override
    public @NonNull String supportedMessageType() {
        return FcbEventOperationType.CLOSE_PAID_OFF.getCode();
    }

    @Override
    public @NonNull HandlerResult handle(@NonNull InboundMessage message) {
        String eventUid = "unknown";
        ClosePaidLoanFacilityMessage paymentMessage;

        try {
            JsonNode rootNode = objectMapper.readTree(message.payload());
            eventUid = rootNode.path("eventUid").asString(eventUid);
            paymentMessage = objectMapper.treeToValue(rootNode, ClosePaidLoanFacilityMessage.class);
        } catch (Exception e) {
            LOG.error("Malformed CLOSE_PAID_OFF message [eventUid={}]", eventUid, e);
            return HandlerResult.permanent(e);
        }

        LOG.info(
                "Processing CLOSE_PAID_OFF from Inbox [eventUid={}, fileNumber={}]",
                eventUid,
                paymentMessage.fileNumber());

        try {
            CloseFacilityPaidOffCommand command = messageMapper.toCommand(paymentMessage);
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
                    "Failed to process CLOSE_PAID_OFF [eventUid={}, fileNumber={}]",
                    eventUid,
                    paymentMessage.fileNumber(),
                    e);
            return HandlerResult.retryable(e);
        }
    }
}
