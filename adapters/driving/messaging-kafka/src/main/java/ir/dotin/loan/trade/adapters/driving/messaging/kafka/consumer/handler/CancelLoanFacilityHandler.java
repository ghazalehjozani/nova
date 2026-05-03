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
import ir.dotin.loan.trade.adapters.driving.contract.dto.CancelFacilityRequest;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.CancelFacilityRequestToCommandMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CancelFacilityCommand;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CancelLoanFacilityHandler implements InboxMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CancelLoanFacilityHandler.class);

    private final ObjectMapper objectMapper;
    private final CommandDispatcher dispatcher;
    private final CancelFacilityRequestToCommandMapper messageMapper;

    @Override
    public @NonNull HandlerResult handle(@NonNull InboundMessage message) {
        {
            String eventUid = "unknown";
            CancelFacilityRequest cancelFacilityMessage;

            try {
                JsonNode rootNode = objectMapper.readTree(message.payload());
                eventUid = rootNode.path("eventUid").asString(eventUid);
                cancelFacilityMessage = objectMapper.treeToValue(rootNode, CancelFacilityRequest.class);
            } catch (Exception e) {
                LOG.error("Malformed CANCELLATION_LOAN_FACILITY message [eventUid={}]", eventUid, e);
                return HandlerResult.permanent(e);
            }

            LOG.info(
                    "Processing CANCELLATION_LOAN_FACILITY from Inbox [eventUid={}, fileNumber={}]",
                    eventUid,
                    cancelFacilityMessage.fileNumber());

            try {
                CancelFacilityCommand command = messageMapper.toCommand(cancelFacilityMessage);
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
                        "Failed to process INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}]",
                        eventUid,
                        cancelFacilityMessage.fileNumber(),
                        e);
                return HandlerResult.retryable(e);
            }
        }
    }

    @Override
    public @NonNull String supportedMessageType() {
        return FcbEventOperationType.CANCEL_LOAN_FACILITY.getCode();
    }
}
