package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.pangaea.saga.api.error.SagaErrors;
import ir.dotin.platform.pangaea.saga.api.model.SagaResult;
import ir.dotin.platform.pangaea.saga.api.orchestration.SagaOrchestrator;
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

        Result<?> validationResult = facilityValidator.callAndValidateServices(command, context);
        if (validationResult.isFailure()) {
            return Result.failure(validationResult.err().orElseThrow());
        }

        Result<Boolean> eligibilityValidation =
                facilityContractValidation.validateForContractIssuance(context.facility(), context.arrangement());
        if (eligibilityValidation.isFailure()) {
            return Result.failure(eligibilityValidation.err().orElseThrow());
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

        var input = IssueFacilityContractInput.of(
                command.loanFacilityId(), command.branchCode(), transactionConfig, command.version());

        SagaResult<IssueFacilityContractSagaData> sagaResult = sagaOrchestrator.executeSaga(
                "issue-facility-contract", input, command.uid().toString());

        log.info("Saga completed: sagaId={}, success={}", sagaResult.sagaId(), sagaResult.isSuccess());

        if (sagaResult.isSuccess()) {
            List<DomainEvent<?>> domainEvents = buildDomainEvents(sagaResult.dataOrNull());
            return Result.success(domainEvents);
        }

        return sagaResult
                .error()
                .map(this::toResult)
                .orElseGet(() -> Result.failure(SagaErrors.COMPENSATED, extractReason(sagaResult)));
    }

    private List<DomainEvent<?>> buildDomainEvents(@Nullable IssueFacilityContractSagaData data) {
        if (data == null
                || data.capturedEvents() == null
                || data.capturedEvents().isEmpty()) {
            return List.of();
        }

        //noinspection unchecked
        return (List<DomainEvent<?>>) (List<?>) data.capturedEvents().stream()
                .map(eventData -> (DomainEvent<?>) TradeLoanFacilityContractIssued.builder(clock)
                        .facilityId(eventData.facilityId())
                        .sanctionedLoanId(Objects.requireNonNull(eventData.sanctionedLoanId(), "sanctionedLoanId"))
                        .transactionNumber(Objects.requireNonNull(eventData.transactionNumber(), "transactionNumber"))
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
        return "Unknown error";
    }

    private Result<List<DomainEvent<?>>> toResult(FailureCause failureCause) {
        return Result.failure(failureCause);
    }
}
