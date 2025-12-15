package ir.dotin.loan.trade.adapters.driving.messaging.consumer;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.command.model.CommandHeaders;
import ir.dotin.platform.adapter.messaging.command.model.CommandProcessingConfig;
import ir.dotin.platform.adapter.messaging.command.model.RawCommandMessage;
import ir.dotin.platform.adapter.messaging.command.processor.CommandProcessor;
import ir.dotin.platform.commons.security.AuthenticationContextHolder;
import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TradeLoanCommandConsumer {

    private static final CommandProcessingConfig CONFIG = CommandProcessingConfig.of("LOAN");

    private final CommandProcessor processor;
    private final FullLoanFacilityLifecycleMessageMapper mapper;
    private final AuthenticationContextHolder authContext;

    @KafkaListener(
            topics = "corridor.loan.trade.full-lifecycle.command.request.queue.v1",
            groupId = "trade-loan-command-consumer",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(
            @Payload String payload,
            @Headers Map<String, Object> headers,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        processor.process(
                RawCommandMessage.of(payload, headers, topic),
                FullLoanFacilityLifecycleMessage.class,
                this::createCommand,
                CONFIG,
                ack);
    }

    private Command createCommand(CommandHeaders headers, FullLoanFacilityLifecycleMessage payload) {
        return mapper.toCommand(payload).toBuilder()
                .uid(headers.requestId())
                .transactionMetadata(buildTransactionMetadata())
                .build();
    }

    private FullLoanFacilityLifecycleCommand.TransactionMetadataDto buildTransactionMetadata() {
        return FullLoanFacilityLifecycleCommand.TransactionMetadataDto.builder()
                .branchCode(authContext.branchCode().orElseThrow())
                .userId(authContext.userIdOrThrow())
                .terminalIp(authContext.ipAddress().orElseThrow())
                .terminalId("1")
                .productCode("LOAN")
                .channel("Branch")
                .networkType("BankBook")
                .terminalType("Branch")
                .toolSource("BANK")
                .build();
    }
}
