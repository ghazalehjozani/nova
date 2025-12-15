package ir.dotin.loan.trade.adapters.driving.messaging.handler;

import java.util.Map;

import org.springframework.stereotype.Component;

import ir.dotin.platform.adapter.messaging.inbox.handler.CommandMessageHandler;
import ir.dotin.platform.adapter.messaging.inbox.model.CommandHeaders;
import ir.dotin.platform.adapter.messaging.inbox.model.CommandResponseEnvelope;
import ir.dotin.platform.commons.security.AuthenticationContextHolder;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.trade.adapters.driving.messaging.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.messaging.mapper.FullLoanFacilityLifecycleMessageMapper;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullLoanFacilityLifecycleMessageHandler
        implements CommandMessageHandler<FullLoanFacilityLifecycleMessage> {

    public static final String COMMAND_TYPE = "FullLoanFacilityLifecycle";

    private final CommandDispatcher dispatcher;
    private final FullLoanFacilityLifecycleMessageMapper mapper;
    private final AuthenticationContextHolder authenticationContextHolder;

    @Override
    public String getCommandType() {
        return COMMAND_TYPE;
    }

    @Override
    public CommandResponseEnvelope handle(CommandHeaders headers, FullLoanFacilityLifecycleMessage payload) {
        log.info("Processing FullLoanFacilityLifecycle command: idempotencyKey={}", headers.idempotencyKey());

        var command = mapper.toCommand(payload).toBuilder()
                .uid(headers.requestId())
                .transactionMetadata(buildTransactionContext())
                .build();

        var result = dispatcher.dispatch(command);

        var events = result.payload();
        Map<String, Object> resultData = Map.of("events", events, "commandUid", command.uid());

        return CommandResponseEnvelope.created(
                headers.idempotencyKey(), headers.requestDateTime(), resultData, "عملیات با موفقیت انجام شد");
    }

    private FullLoanFacilityLifecycleCommand.TransactionMetadataDto buildTransactionContext() {
        return FullLoanFacilityLifecycleCommand.TransactionMetadataDto.builder()
                .branchCode(authenticationContextHolder.branchCode().orElseThrow())
                .userId(authenticationContextHolder.userIdOrThrow())
                .terminalIp(authenticationContextHolder.ipAddress().orElseThrow())
                .terminalId("1")
                .productCode("LOAN")
                .channel("Branch")
                .networkType("BankBook")
                .terminalType("Branch")
                .toolSource("BANK")
                .build();
    }
}
