package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.dispatcher.api.execution.ExecutionResult;
import ir.dotin.platform.pangaea.inbox.api.HandlerResult;
import ir.dotin.platform.pangaea.inbox.api.InboxMessageHandler;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentPaymentMessage;
import ir.dotin.loan.trade.adapters.driving.contract.mapper.InstallmentCollectionMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CollectInstallmentCommand;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class InstallmentCollectionHandler implements InboxMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentCollectionHandler.class);

    private final ObjectMapper objectMapper;
    private final InstallmentCollectionMessageMapper messageMapper;
    private final CommandDispatcher dispatcher;

    @Override
    public @NonNull String supportedMessageType() {
        return FcbEventOperationType.INSTALLMENT_COLLECTION.getCode();
    }

    @Override
    public @NonNull HandlerResult handle(@NonNull InboundMessage message) {
        UUID eventUid = message.headers().eventUid();
        InstallmentPaymentMessage paymentMessage;

        try {
            paymentMessage = objectMapper.readValue(message.payload(), InstallmentPaymentMessage.class);
        } catch (Exception e) {
            LOG.error("Malformed INSTALLMENT_COLLECTION message [eventUid={}]", eventUid, e);
            return HandlerResult.permanent(e);
        }

        LOG.info(
                "Processing INSTALLMENT_COLLECTION from Inbox [eventUid={}, fileNumber={}]",
                eventUid,
                paymentMessage.fileNumber());

        try {
            CollectInstallmentCommand command = messageMapper.toCommand(paymentMessage);
            ExecutionResult<?> executionResult = dispatcher.dispatch(command);

            return switch (executionResult) {
                case ExecutionResult.Fresh<?> ignored -> HandlerResult.success();
                case ExecutionResult.Replayed<?> ignored -> HandlerResult.success();
            };

        } catch (FailureCauseException e) {
            LOG.error(
                    "Domain rule rejected INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}]",
                    eventUid,
                    paymentMessage.fileNumber(),
                    e);
            return HandlerResult.permanent(e);
        } catch (Exception e) {
            LOG.error(
                    "Failed to process INSTALLMENT_COLLECTION [eventUid={}, fileNumber={}]",
                    eventUid,
                    paymentMessage.fileNumber(),
                    e);
            return HandlerResult.retryable(e);
        }
    }
}
