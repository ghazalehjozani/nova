package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.saga.api.orchestration.SagaOrchestrator;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga.IssueFacilityContractInput;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueFacilityContractCommandHandler implements CommandHandler<IssueFacilityContractCommand> {

    private static final Logger log = LoggerFactory.getLogger(IssueFacilityContractCommandHandler.class);
    private final SagaOrchestrator<?> sagaOrchestrator;

    @Override
    public Result<List<DomainEvent<?>>> handle(IssueFacilityContractCommand command) {

        TransactionConfig transactionConfig = TransactionConfig.builder()
                .userId(command.userId())
                .branchCode(command.branchCode())
                .terminalId(command.terminalId())
                .terminalIp(command.terminalIp())
                .terminalType(command.terminalType())
                .channel(command.channel())
                .toolSource(command.toolSource())
                .productCode(command.productCode())
                .networkType(command.networkType())
                .build();

        var input = IssueFacilityContractInput.of(command.loanFacilityId(), command.branchCode(), transactionConfig);

        var sagaId = sagaOrchestrator.startSaga(
                "issue-facility-contract", input, command.loanFacilityId().toString());

        log.info("Started saga: {}", sagaId);
        return Result.success(List.of());
    }
}
