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
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.servicelayer.api.execution.ExecutionResult;
import ir.dotin.loan.trade.adapters.driving.contract.dto.ClosePaidLoanFacilityMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.ClosePaidLoanFacilityMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CloseFacilityPaidOffCommand;

import lombok.RequiredArgsConstructor;
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
        UUID eventUid = message.headers().eventUid();
        ClosePaidLoanFacilityMessage paymentMessage;

        try {
            paymentMessage = objectMapper.readValue(message.payload(), ClosePaidLoanFacilityMessage.class);
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
            };

        } catch (FailureCauseException e) {
            LOG.error(
                    "Domain rule rejected CLOSE_PAID_OFF [eventUid={}, fileNumber={}]",
                    eventUid,
                    paymentMessage.fileNumber(),
                    e);
            return HandlerResult.permanent(e);
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
