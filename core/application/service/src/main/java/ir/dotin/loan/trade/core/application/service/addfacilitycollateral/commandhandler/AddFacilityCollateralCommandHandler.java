package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.platform.pangaea.saga.api.handler.SagaCommandHandler;
import ir.dotin.platform.pangaea.saga.api.orchestration.SagaOrchestrator;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.AddFacilityCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralValidationContext;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.saga.AddFacilityCollateralInput;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.saga.AddFacilityCollateralSagaData;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

@Service
public class AddFacilityCollateralCommandHandler
        extends SagaCommandHandler<AddFacilityCollateralCommand, AddFacilityCollateralSagaData> {

    private static final Logger log = LoggerFactory.getLogger(AddFacilityCollateralCommandHandler.class);

    private final AddFacilityCollateralCommandMapper mapper;
    private final AddFacilityCollateralDependencyLoader dependencyLoader;
    private final BranchAccessValidator branchAccessValidator;

    public AddFacilityCollateralCommandHandler(
            SagaOrchestrator<AddFacilityCollateralSagaData> sagaOrchestrator,
            AddFacilityCollateralCommandMapper mapper,
            AddFacilityCollateralDependencyLoader dependencyLoader,
            BranchAccessValidator branchAccessValidator) {
        super(sagaOrchestrator);
        this.mapper = mapper;
        this.dependencyLoader = dependencyLoader;
        this.branchAccessValidator = branchAccessValidator;
    }

    @Override
    protected String sagaType() {
        return "add-facility-collateral";
    }

    @Override
    protected Result<SagaInput> prepare(AddFacilityCollateralCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        List<Collateral> collaterals = mapper.toCollaterals(command.collaterals());

        Result<CollateralValidationContext> contextResult =
                dependencyLoader.loadAndCalculate(loanFacilityId, collaterals);
        if (contextResult.isFailure()) {
            return Result.failure(contextResult.err().orElseThrow());
        }
        CollateralValidationContext context = contextResult.unwrap();

        Result<Unit> branchResult =
                branchAccessValidator.verifyCallerCoversFacility(command.branchCode(), context.facility());
        if (branchResult.isFailure()) {
            return Result.failure(branchResult.err().orElseThrow());
        }

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

        return Result.success(buildInput(command));
    }

    private SagaInput buildInput(AddFacilityCollateralCommand command) {
        return AddFacilityCollateralInput.of(
                command.loanFacilityId(), command.uid(), command.collaterals(), command.version());
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
}
