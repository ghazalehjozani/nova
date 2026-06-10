package ir.dotin.loan.trade.adapters.driving.messaging.kafka.consumer.handler;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.inbox.api.HandlerResult;
import ir.dotin.platform.pangaea.inbox.api.InboxMessageHandler;
import ir.dotin.platform.pangaea.messaging.api.inbound.InboundMessage;
import ir.dotin.platform.pangaea.servicelayer.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.pangaea.servicelayer.api.execution.ExecutionResult;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FcbEventOperationType;
import ir.dotin.loan.trade.adapters.driving.contract.dto.InstallmentCollectionCompensateMessage;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollectInstallmentCommand;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class InstallmentCollectionCompensateHandler implements InboxMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentCollectionCompensateHandler.class);

    private final ObjectMapper objectMapper;
    private final CommandDispatcher dispatcher;

    @Override
    public @NonNull String supportedMessageType() {
        return FcbEventOperationType.INSTALLMENT_COLLECTION_COMPENSATE.getCode();
    }

    @Override
    public @NonNull HandlerResult handle(@NonNull InboundMessage message) {
        InstallmentCollectionCompensateMessage compensateMessage;
        UUID eventUid = message.headers().eventUid();

        if (eventUid == null) {
            LOG.error("Missing eventUid header on INSTALLMENT_COLLECTION_COMPENSATE message");
            return HandlerResult.permanent(new IllegalStateException("Missing eventUid header"));
        }

        try {
            compensateMessage = objectMapper.readValue(message.payload(), InstallmentCollectionCompensateMessage.class);
        } catch (Exception e) {
            LOG.error("Malformed INSTALLMENT_COLLECTION_COMPENSATE message [eventUid={}]", eventUid, e);
            return HandlerResult.permanent(e);
        }

        LOG.info(
                "Processing INSTALLMENT_COLLECTION_COMPENSATE from Inbox [eventUid={}, fileNumber={}]",
                eventUid,
                compensateMessage.fileNumber());

        try {
            CompensateCollectInstallmentCommand command = CompensateCollectInstallmentCommand.builder()
                    .uid(eventUid)
                    .applicationNumber(compensateMessage.fileNumber())
                    .transactionNumbers(compensateMessage.transactionNumbers())
                    .build();

            ExecutionResult<List<DomainEvent<?>>> executionResult = dispatcher.dispatch(command);

            return switch (executionResult) {
                case ExecutionResult.Fresh<?> fresh -> HandlerResult.success();
                case ExecutionResult.Replayed<?> replayed -> HandlerResult.success();
            };

        } catch (FailureCauseException e) {
            LOG.error(
                    "Domain rule rejected INSTALLMENT_COLLECTION_COMPENSATE [eventUid={}, fileNumber={}]",
                    eventUid,
                    compensateMessage.fileNumber(),
                    e);
            return HandlerResult.permanent(e);
        } catch (Exception e) {
            LOG.error(
                    "Failed to process INSTALLMENT_COLLECTION_COMPENSATE [eventUid={}, fileNumber={}]",
                    eventUid,
                    compensateMessage.fileNumber(),
                    e);
            return HandlerResult.retryable(e);
        }
    }
}
