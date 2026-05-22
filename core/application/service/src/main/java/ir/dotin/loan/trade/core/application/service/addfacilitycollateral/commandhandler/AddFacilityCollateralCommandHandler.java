package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.Unit;
import ir.dotin.platform.commons.core.error.FailureCause;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.saga.api.error.SagaErrors;
import ir.dotin.platform.saga.api.exception.SagaSuspendedException;
import ir.dotin.platform.saga.api.model.SagaResult;
import ir.dotin.platform.saga.api.orchestration.SagaOrchestrator;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.AddFacilityCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralValidationContext;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.saga.AddFacilityCollateralInput;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.saga.AddFacilityCollateralSagaData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCollateralAdded;

import lombok.RequiredArgsConstructor;

/**
 * Command handler for add-facility-collateral (LN-59321).
 *
 * <p>Performs the read-only pre-saga work — dependency loading, per-collateral adequacy and total-value checks — then
 * delegates the distributed reserve + add sequence to {@code AddFacilityCollateralSaga} via the
 * {@link SagaOrchestrator}. The {@code AddFacilityCollateralCommand} contract is unchanged, so the full-lifecycle saga
 * and REST controller keep dispatching it as before; this handler now triggers an internal sub-saga (mirrors
 * {@code IssueFacilityContractCommandHandler}).
 */
@Service
@RequiredArgsConstructor
public class AddFacilityCollateralCommandHandler implements CommandHandler<AddFacilityCollateralCommand> {

    private static final Logger log = LoggerFactory.getLogger(AddFacilityCollateralCommandHandler.class);

    private final AddFacilityCollateralCommandMapper mapper;
    private final AddFacilityCollateralDependencyLoader dependencyLoader;
    private final SagaOrchestrator<AddFacilityCollateralSagaData> sagaOrchestrator;

    @Override
    public Result<List<DomainEvent<?>>> handle(AddFacilityCollateralCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        List<Collateral> collaterals = mapper.toCollaterals(command.collaterals());

        Result<CollateralValidationContext> contextResult =
                dependencyLoader.loadAndCalculate(loanFacilityId, collaterals);
        if (contextResult.isFailure()) {
            return Result.failure(contextResult.err().orElseThrow());
        }
        CollateralValidationContext context = contextResult.unwrap();
        Money requiredAmount = Objects.requireNonNull(context).requiredCollateralAmount();

        Result<Unit> adequacyResult = validateCollateralAdequacy(collaterals, context);
        if (adequacyResult.isFailure()) {
            return Result.failure(adequacyResult.err().orElseThrow());
        }

        Money totalNewCollateralAmount = collaterals.stream()
                .map(Collateral::usedAmount)
                .reduce(Money.zero(context.arrangement().getCurrencyType()).unwrap(), (a, b) -> a.add(b)
                        .unwrap());

        Result<Unit> valueValidationResult = validateTotalCollateralValue(totalNewCollateralAmount, requiredAmount);
        if (valueValidationResult.isFailure()) {
            return Result.failure(valueValidationResult.err().orElseThrow());
        }

        return runSaga(command);
    }

    private Result<List<DomainEvent<?>>> runSaga(AddFacilityCollateralCommand command) {
        var input = AddFacilityCollateralInput.of(command.loanFacilityId(), command.uid(), command.collaterals());

        SagaResult<AddFacilityCollateralSagaData> sagaResult = sagaOrchestrator.executeSaga(
                "add-facility-collateral", input, command.uid().toString());

        log.info("Saga completed: sagaId={}, success={}", sagaResult.sagaId(), sagaResult.isSuccess());

        if (sagaResult.isSuspended()) {
            var suspended = (SagaResult.Suspended<AddFacilityCollateralSagaData>) sagaResult;
            throw new SagaSuspendedException(sagaResult.sagaId(), extractReason(sagaResult), suspended.reason());
        }

        if (sagaResult.isSuccess()) {
            return Result.success(buildDomainEvents(sagaResult.dataOrNull()));
        }

        return sagaResult
                .error()
                .map(this::toResult)
                .orElseGet(() -> Result.failure(SagaErrors.COMPENSATED, extractReason(sagaResult)));
    }

    private List<DomainEvent<?>> buildDomainEvents(AddFacilityCollateralSagaData data) {
        if (data == null
                || data.capturedEvents() == null
                || data.capturedEvents().isEmpty()) {
            return List.of();
        }

        //noinspection unchecked
        return (List<DomainEvent<?>>) (List<?>) data.capturedEvents().stream()
                .map(eventData -> (DomainEvent<?>) new TradeLoanFacilityCollateralAdded(
                        eventData.eventId(),
                        eventData.aggregateId(),
                        eventData.eventType(),
                        eventData.sanctionedLoanId(),
                        eventData.collateralSerials(),
                        eventData.createdAt()))
                .toList();
    }

    private Result<Unit> validateCollateralAdequacy(List<Collateral> collaterals, CollateralValidationContext context) {
        for (Collateral collateral : collaterals) {
            CollateralDetails details = context.collateralDetailsMap().get(collateral.collateralSerial());
            if (details == null) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.COLLATERAL_DETAILS_NOT_FOUND,
                        collateral.collateralSerial().value()));
            }

            Money realCollateralPrice = Money.valueOf(
                            details.price(), context.arrangement().getCurrencyType())
                    .unwrap();

            if (collateral.usedAmount().isGreaterThan(realCollateralPrice).unwrap()) {
                log.warn(
                        "Collateral adequacy validation failed for serial {}",
                        collateral.collateralSerial().value());
                return Result.failure(
                        TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE,
                        realCollateralPrice,
                        collateral.usedAmount());
            }
        }
        return Result.success();
    }

    private Result<Unit> validateTotalCollateralValue(Money totalValue, Money requiredAmount) {
        if (totalValue.isLessThan(requiredAmount).unwrap()) {
            log.warn("Total new collateral value {} is less than required amount {}", totalValue, requiredAmount);
            return Result.failure(
                    TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE, totalValue, requiredAmount);
        }
        return Result.success();
    }

    private String extractReason(SagaResult<AddFacilityCollateralSagaData> sagaResult) {
        if (sagaResult instanceof SagaResult.Compensated<AddFacilityCollateralSagaData> c) {
            return c.reason();
        }
        if (sagaResult instanceof SagaResult.Failed<AddFacilityCollateralSagaData> f) {
            return f.reason();
        }
        if (sagaResult instanceof SagaResult.Suspended<AddFacilityCollateralSagaData> s) {
            return s.reason();
        }
        return "Unknown error";
    }

    private Result<List<DomainEvent<?>>> toResult(FailureCause failureCause) {
        return Result.failure(failureCause);
    }
}
