package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.commandhandler;

import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.context.InvocationContextHolder;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.pangaea.saga.api.error.SagaErrors;
import ir.dotin.platform.pangaea.saga.api.exception.SagaSuspendedException;
import ir.dotin.platform.pangaea.saga.api.model.SagaResult;
import ir.dotin.platform.pangaea.saga.api.orchestration.SagaOrchestrator;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.service.fullloanlifecycle.mapper.FullLoanFacilityLifecycleCommandMapper;
import ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga.FullLoanFacilityLifecycleInput;
import ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga.FullLoanFacilityLifecycleSagaData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FullLoanFacilityLifecycleCommandHandler implements CommandHandler<FullLoanFacilityLifecycleCommand> {

    private final SagaOrchestrator<FullLoanFacilityLifecycleSagaData> sagaOrchestrator;
    private final FullLoanFacilityLifecycleCommandMapper mapper;

    @Override
    public Result<List<DomainEvent<?>>> handle(FullLoanFacilityLifecycleCommand command) {
        log.info("Starting full loan facility lifecycle saga for correlation: {}", command.uid());

        OriginateLoanFacilityCommand originationCommand = mapper.toOriginationCommand(command);
        var transactionConfig = mapper.toTransactionConfig(command.transactionMetadata());
        var disbursementMethod = command.loanApplication().disbursementMethod();
        var disbursementDate = command.disbursement().disbursementDate();

        var input = FullLoanFacilityLifecycleInput.of(
                originationCommand,
                command.collaterals(),
                command.disbursement().trancheAmount().value(),
                command.transactionMetadata().branchCode(),
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                command.uid(),
                command.confirmType());

        String sagaCorrelationId = InvocationContextHolder.current().flow().flowCorrelationId();

        SagaResult<FullLoanFacilityLifecycleSagaData> sagaResult =
                sagaOrchestrator.executeSaga("full-loan-facility-lifecycle", input, sagaCorrelationId);

        log.info("Saga completed: sagaId={}, success={}", sagaResult.sagaId(), sagaResult.isSuccess());

        if (sagaResult.isSuspended()) {
            var suspended = (SagaResult.Suspended<FullLoanFacilityLifecycleSagaData>) sagaResult;
            throw new SagaSuspendedException(sagaResult.sagaId(), extractSuspendedStep(sagaResult), suspended.reason());
        }

        if (sagaResult.isSuccess()) {
            // Return NO events. Each saga step dispatches its own command, which publishes that step's
            // domain events to the outbox inside the step's own (REQUIRED) transaction — they are already
            // persisted. This orchestration command is a NonTransactionalCommand, so the dispatcher opens
            // no transaction around it; returning the collected events here would make the dispatcher
            // re-publish them with no active transaction, and the MANDATORY OutboxEventListener would throw
            // IllegalTransactionStateException (LN-59391). The collected events on the saga data remain
            // available for in-saga decisions (id extraction, compensation); they must not be re-published.
            return Result.success(List.of());
        }

        return sagaResult
                .error()
                .map(this::toResult)
                .orElseGet(() -> Result.failure(SagaErrors.COMPENSATED, extractReason(sagaResult)));
    }

    private Result<List<DomainEvent<?>>> toResult(FailureCause failureCause) {
        return Result.failure(failureCause);
    }

    private String extractReason(SagaResult<FullLoanFacilityLifecycleSagaData> sagaResult) {
        if (sagaResult instanceof SagaResult.Compensated<FullLoanFacilityLifecycleSagaData> c) {
            return c.reason();
        }
        if (sagaResult instanceof SagaResult.Failed<FullLoanFacilityLifecycleSagaData> f) {
            return f.reason();
        }
        if (sagaResult instanceof SagaResult.Suspended<FullLoanFacilityLifecycleSagaData> s) {
            return s.reason();
        }
        return "Unknown error";
    }

    private String extractSuspendedStep(SagaResult<FullLoanFacilityLifecycleSagaData> sagaResult) {
        if (sagaResult instanceof SagaResult.Suspended<FullLoanFacilityLifecycleSagaData> s) {
            return s.reason();
        }
        return "unknown";
    }
}
