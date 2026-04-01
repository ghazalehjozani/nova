package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import java.time.Clock;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.saga.api.error.SagaErrors;
import ir.dotin.platform.saga.api.exception.SagaSuspendedException;
import ir.dotin.platform.saga.api.model.SagaResult;
import ir.dotin.platform.saga.api.model.StepError;
import ir.dotin.platform.saga.api.orchestration.SagaOrchestrator;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractDependencyLoader;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractValidator;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga.IssueFacilityContractInput;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga.IssueFacilityContractSagaData;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.strategy.FacilityContractContext;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityContractIssued;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.FacilityContractValidation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueFacilityContractCommandHandler implements CommandHandler<IssueFacilityContractCommand> {

    private static final Logger log = LoggerFactory.getLogger(IssueFacilityContractCommandHandler.class);
    private final FacilityContractDependencyLoader dependencyLoader;
    private final FacilityContractValidator facilityValidator;
    private final FacilityContractValidation facilityContractValidation;
    private final SagaOrchestrator<IssueFacilityContractSagaData> sagaOrchestrator;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?>>> handle(IssueFacilityContractCommand command) {
        return dependencyLoader
                .loadDependencies(command)
                .flatMap(context -> executeContractIssuanceWorkflow(command, context));
    }

    private Result<List<DomainEvent<?>>> executeContractIssuanceWorkflow(
            IssueFacilityContractCommand command, FacilityContractContext context) {

        Result<Void> validationResult = facilityValidator.callAndValidateServices(command, context);
        if (validationResult.isFailure()) {
            return Result.failure(validationResult.notification());
        }

        Result<Boolean> eligibilityValidation =
                facilityContractValidation.validateForContractIssuance(context.facility(), context.arrangement());
        if (eligibilityValidation.isFailure()) {
            return Result.failure(eligibilityValidation.notification());
        }

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

        SagaResult<IssueFacilityContractSagaData> sagaResult = sagaOrchestrator.executeSaga(
                "issue-facility-contract", input, command.uid().toString());

        log.info("Saga completed: sagaId={}, success={}", sagaResult.sagaId(), sagaResult.isSuccess());

        if (sagaResult.isSuspended()) {
            var suspended = (SagaResult.Suspended<IssueFacilityContractSagaData>) sagaResult;
            throw new SagaSuspendedException(sagaResult.sagaId(), extractSuspendedStep(sagaResult), suspended.reason());
        }

        if (sagaResult.isSuccess()) {
            List<DomainEvent<?>> domainEvents = buildDomainEvents(sagaResult.dataOrNull());
            return Result.success(domainEvents);
        }

        return sagaResult
                .error()
                .map(this::toResult)
                .orElseGet(
                        () -> Result.failure(Notification.ofError(SagaErrors.COMPENSATED, extractReason(sagaResult))));
    }

    private List<DomainEvent<?>> buildDomainEvents(IssueFacilityContractSagaData data) {
        if (data == null
                || data.capturedEvents() == null
                || data.capturedEvents().isEmpty()) {
            return List.of();
        }

        //noinspection unchecked
        return (List<DomainEvent<?>>) (List<?>) data.capturedEvents().stream()
                .map(eventData -> (DomainEvent<?>) TradeLoanFacilityContractIssued.builder(clock)
                        .facilityId(eventData.facilityId())
                        .sanctionedLoanId(eventData.sanctionedLoanId())
                        .transactionNumber(eventData.transactionNumber())
                        .occurredAt(eventData.occurredAt())
                        .build())
                .toList();
    }

    private String extractReason(SagaResult<IssueFacilityContractSagaData> sagaResult) {
        if (sagaResult instanceof SagaResult.Compensated<IssueFacilityContractSagaData> c) {
            return c.reason();
        }
        if (sagaResult instanceof SagaResult.Failed<IssueFacilityContractSagaData> f) {
            return f.reason();
        }
        if (sagaResult instanceof SagaResult.Suspended<IssueFacilityContractSagaData> s) {
            return s.reason();
        }
        return "Unknown error";
    }

    private String extractSuspendedStep(SagaResult<IssueFacilityContractSagaData> sagaResult) {
        if (sagaResult instanceof SagaResult.Suspended<IssueFacilityContractSagaData> s) {
            return s.reason();
        }
        return "unknown";
    }

    private Result<List<DomainEvent<?>>> toResult(StepError stepError) {
        return switch (stepError) {
            case StepError.BusinessRuleError bre -> Result.failure(bre.notification());
            case StepError.ValidationError ve ->
                Result.failure(Notification.ofError(SagaErrors.VALIDATION_FAILED, ve.message()));
            case StepError.BusinessError be ->
                Result.failure(Notification.ofError(SagaErrors.STEP_FAILED, be.message()));
            case StepError.TechnicalError te ->
                Result.failure(Notification.ofError(SagaErrors.TECHNICAL_ERROR, te.message()));
            case StepError.TimeoutError toe ->
                Result.failure(Notification.ofError(SagaErrors.TIMEOUT, toe.timeoutMillis()));
        };
    }
}
