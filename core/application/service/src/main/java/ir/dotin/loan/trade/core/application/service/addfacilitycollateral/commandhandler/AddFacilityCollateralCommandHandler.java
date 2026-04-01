package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.AbstractCollateralValidationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.AddFacilityCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralValidationContext;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddFacilityCollateralCommandHandler implements CommandHandler<AddFacilityCollateralCommand> {

    private static final Logger log = LoggerFactory.getLogger(AddFacilityCollateralCommandHandler.class);

    private final AddFacilityCollateralCommandMapper mapper;
    private final TradeLoanFacilityRepository repository;
    private final TradeLoanFacilityService domainService;
    private final AddFacilityCollateralDependencyLoader dependencyLoader;
    private final AbstractCollateralValidationService collateralValidationService;
    private final CollateralServicePort collateralServicePort;

    private static final Integer RESERVE_DURATION_MINUTES = 1440;

    @Override
    public Result<List<DomainEvent<?>>> handle(AddFacilityCollateralCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        List<Collateral> collaterals = mapper.toCollaterals(command.collaterals());

        Result<CollateralValidationContext> contextResult =
                dependencyLoader.loadAndCalculate(loanFacilityId, collaterals);

        if (contextResult.isFailure()) {
            return Result.failure(contextResult.notification());
        }
        CollateralValidationContext context = contextResult.value();
        Money requiredAmount = Objects.requireNonNull(context).requiredCollateralAmount();

        for (Collateral collateral : collaterals) {
            CollateralDetails details = context.collateralDetailsMap().get(collateral.collateralSerial());
            if (details == null) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.COLLATERAL_DETAILS_NOT_FOUND,
                        collateral.collateralSerial().value()));
            }

            Money collateralUsedAmount = collateral.usedAmount();
            Money realCollateralPrice = Money.valueOf(
                            details.price(), context.arrangement().getCurrencyType())
                    .orElseThrow();

            Result<Void> adequacyResult = validateCollateralAdequacy(collateralUsedAmount, realCollateralPrice);
            if (adequacyResult.isFailure()) {
                log.warn(
                        "Collateral adequacy validation failed for serial {}",
                        collateral.collateralSerial().value());
                return Result.failure(adequacyResult.notification());
            }
        }

        Money totalNewCollateralAmount = collaterals.stream()
                .map(Collateral::usedAmount)
                .reduce(Money.zero(context.arrangement().getCurrencyType()).orElseThrow(), (a, b) -> a.add(b)
                        .orElseThrow());

        Result<Void> valueValidationResult = validateTotalCollateralValue(totalNewCollateralAmount, requiredAmount);
        if (valueValidationResult.isFailure()) {
            return Result.failure(valueValidationResult.notification());
        }

        TradeLoanFacility facility = context.facility();

        Result<Void> reservationResult = reserveCollaterals(collaterals, facility, command.uid());
        if (reservationResult.isFailure()) {
            return Result.failure(reservationResult.notification());
        }

        try {
            return domainService
                    .addCollateral(facility, collaterals)
                    .map(v -> facility)
                    .flatMap(f -> {
                        var validation =
                                collateralValidationService.validateFacilityCollaterals(f, context.arrangement());
                        return validation.isFailure() ? Result.failure(validation.notification()) : Result.success(f);
                    })
                    .peekValue(f -> {
                        repository.save(f);
                        log.info(
                                "{} collaterals added and reserved for facility: {}",
                                collaterals.size(),
                                command.loanFacilityId());
                    })
                    .mapNonNull(TradeLoanFacility::domainEvents);
        } catch (Exception e) {
            rollbackReservations(
                    collaterals,
                    facility.getLoanApplication().getApplicationNumber().orElseThrow(),
                    command.uid());
            return Result.failure(
                    Notification.ofError(TradeLoanApplicationServiceErrors.ADD_COLLATERAL_PROCESS_COULD_NOT_COMPLETE));
        }
    }

    private Result<Void> validateTotalCollateralValue(Money totalValue, Money requiredAmount) {
        if (Boolean.TRUE.equals(totalValue.isLessThan(requiredAmount).value())) {
            log.warn("Total new collateral value {} is less than required amount {}", totalValue, requiredAmount);
            return Result.failure(Notification.ofError(
                    TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE, totalValue, requiredAmount));
        }
        return Result.success();
    }

    private Result<Void> validateCollateralAdequacy(Money usedAmount, Money realCollateralPrice) {
        if (Boolean.TRUE.equals(usedAmount.isGreaterThan(realCollateralPrice).value())) {
            return Result.failure(Notification.ofError(
                    TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE, realCollateralPrice, usedAmount));
        }
        return Result.success();
    }

    private Result<Void> reserveCollaterals(List<Collateral> collaterals, TradeLoanFacility facility, UUID requestId) {
        List<Collateral> successfulReservations = new ArrayList<>();

        if (facility.getLoanApplication().getApplicationNumber().isEmpty()) {
            return Result.failure(Notification.ofError(TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING));
        }
        ApplicationNumber appNumber =
                facility.getLoanApplication().getApplicationNumber().get();

        for (Collateral collateral : collaterals) {
            Result<List<CollateralSerial>> result = collateralServicePort.reserveCollateral(
                    collateral.collateralSerial(),
                    appNumber,
                    requestId,
                    RESERVE_DURATION_MINUTES,
                    collateral.usedAmount());

            if (result.isSuccess()) {
                successfulReservations.add(collateral);
            } else {
                rollbackReservations(successfulReservations, appNumber, requestId);
                return Result.failure(result.notification());
            }
        }
        return Result.success();
    }

    private void rollbackReservations(
            List<Collateral> collateralsToRollback, ApplicationNumber appNumber, UUID requestId) {
        for (Collateral collateral : collateralsToRollback) {
            collateralServicePort.unReserveCollateral(
                    collateral.collateralSerial(), appNumber, UUID.randomUUID(), requestId);
        }
    }
}
