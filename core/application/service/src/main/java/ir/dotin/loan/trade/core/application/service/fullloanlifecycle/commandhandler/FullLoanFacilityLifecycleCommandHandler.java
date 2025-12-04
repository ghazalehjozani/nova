package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.commandhandler;

import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.saga.api.i18n.SagaErrorCodes;
import ir.dotin.platform.saga.api.model.SagaResult;
import ir.dotin.platform.saga.api.model.StepError;
import ir.dotin.platform.saga.api.orchestration.SagaOrchestrator;
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

        var input = FullLoanFacilityLifecycleInput.of(
                originationCommand,
                command.transactionMetadata().branchCode(),
                transactionConfig,
                disbursementMethod,
                command.uid());

        SagaResult<FullLoanFacilityLifecycleSagaData> sagaResult = sagaOrchestrator.executeSaga(
                "full-loan-facility-lifecycle", input, command.uid().toString());

        log.info("Saga completed: sagaId={}, success={}", sagaResult.sagaId(), sagaResult.isSuccess());

        if (sagaResult.isSuccess()) {
            List<DomainEvent<?>> events =
                    sagaResult.dataOrNull() != null ? sagaResult.dataOrNull().collectedDomainEvents() : List.of();
            return Result.success(events);
        }

        return sagaResult
                .error()
                .map(this::toResult)
                .orElseGet(() -> Result.failure(
                        Notification.ofError(SagaErrorCodes.SAGA_COMPENSATED, extractReason(sagaResult))));
    }

    private Result<List<DomainEvent<?>>> toResult(StepError stepError) {
        return switch (stepError) {
            case StepError.BusinessRuleError bre -> Result.failure(bre.notification());
            case StepError.ValidationError ve ->
                Result.failure(Notification.ofError(SagaErrorCodes.SAGA_VALIDATION_FAILED, ve.message()));
            case StepError.BusinessError be ->
                Result.failure(Notification.ofError(SagaErrorCodes.SAGA_STEP_FAILED, be.message()));
            case StepError.TechnicalError te ->
                Result.failure(Notification.ofError(SagaErrorCodes.SAGA_TECHNICAL_ERROR, te.message()));
            case StepError.TimeoutError toe ->
                Result.failure(Notification.ofError(SagaErrorCodes.SAGA_TIMEOUT, toe.timeoutMillis()));
        };
    }

    private String extractReason(SagaResult<FullLoanFacilityLifecycleSagaData> sagaResult) {
        if (sagaResult instanceof SagaResult.Compensated<FullLoanFacilityLifecycleSagaData> c) {
            return c.reason();
        }
        if (sagaResult instanceof SagaResult.Failed<FullLoanFacilityLifecycleSagaData> f) {
            return f.reason();
        }
        return "Unknown error";
    }
}
