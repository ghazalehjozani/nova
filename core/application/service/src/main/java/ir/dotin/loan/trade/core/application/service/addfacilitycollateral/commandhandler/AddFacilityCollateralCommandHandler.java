package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.AddFacilityCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralValidationContext;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.i18n.AddFacilityCollateralErrorCodes;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
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

    @Override
    public Result<List<DomainEvent<?>>> handle(AddFacilityCollateralCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());
        CollateralSerial collateralSerial = mapper.toCollateralSerial(command.collateralSerialDto());

        Result<CollateralValidationContext> contextResult =
                dependencyLoader.loadAndCalculate(loanFacilityId, collateralSerial);

        if (contextResult.isFailure()) {
            return Result.failure(contextResult.notification());
        }
        CollateralValidationContext context = contextResult.value();

        Money collateralValue = mapper.toMoney(command.usedAmount());
        Result<Void> validationResult = validateCollateralValue(
                collateralValue, Objects.requireNonNull(context).requiredCollateralAmount());

        Money realCollateralPrice = Money.valueOf(
                        context.collateralDetails().price(),
                        context.arrangement().getCurrencyType())
                .orElseThrow();

        Result<Void> adequacyResult = validateCollateralAdequacy(collateralValue, realCollateralPrice);

        if (adequacyResult.isFailure()) {
            log.warn("Collateral adequacy validation failed for facility {}", loanFacilityId);
            return Result.failure(adequacyResult.notification());
        }

        if (validationResult.isFailure()) {
            log.warn(
                    "Collateral value validation failed for facility {}: {}",
                    loanFacilityId,
                    validationResult.notification().errors());
            return Result.failure(validationResult.notification());
        }

        TradeLoanFacility facility = context.facility();

        return domainService
                .addCollateral(facility, collateralSerial)
                .map(v -> facility)
                .peekValue(f -> {
                    repository.save(f);
                    log.debug("Collateral added to facility: {}", command.loanFacilityId());
                })
                .mapNonNull(TradeLoanFacility::domainEvents);
    }

    private Result<Void> validateCollateralValue(Money newCollateralValue, Money requiredAmount) {
        if (Boolean.TRUE.equals(newCollateralValue.isLessThan(requiredAmount).value())) {
            log.warn("New collateral value {} is less than required amount {}", newCollateralValue, requiredAmount);
            return Result.failure(Notification.ofError(
                    AddFacilityCollateralErrorCodes.INSUFFICIENT_COLLATERAL_VALUE, newCollateralValue, requiredAmount));
        }

        return Result.success();
    }

    private Result<Void> validateCollateralAdequacy(Money newCollateralValue, Money realCollateralPrice) {
        if (Boolean.TRUE.equals(
                newCollateralValue.isGreaterThan(realCollateralPrice).value())) {
            return Result.failure(Notification.ofError(
                    AddFacilityCollateralErrorCodes.INSUFFICIENT_COLLATERAL_VALUE,
                    realCollateralPrice,
                    newCollateralValue));
        }
        return Result.success();
    }
}
