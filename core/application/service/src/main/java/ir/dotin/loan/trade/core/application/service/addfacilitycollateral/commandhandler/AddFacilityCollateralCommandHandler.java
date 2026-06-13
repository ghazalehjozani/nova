package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.model.RetryPolicy;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.AddFacilityCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralValidationContext;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.step.AddCollateralStep;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.step.ReserveCollateralsStep;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.AddFacilityCollateralStep;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.CollateralData;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.remote;
import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.writePublishing;

@Service
@RequiredArgsConstructor
public class AddFacilityCollateralCommandHandler
        implements WorkflowCommandHandler<AddFacilityCollateralCommand, CollateralData> {

    @Override
    public Workflow<CollateralData> definition() {
        // @formatter:off
        return Workflow.<CollateralData>named("add-facility-collateral")
                .step(remote(AddFacilityCollateralStep.RESERVE_COLLATERALS, reserveCollateralsStep)
                        .retry(RetryPolicy.CONSERVATIVE)
                        .timeout(Duration.ofSeconds(30)))
                .step(writePublishing(AddFacilityCollateralStep.ADD_COLLATERAL, addCollateralStep))
                .build();
        // @formatter:on
    }

    @Override
    public Result<CollateralData> seed(AddFacilityCollateralCommand command) {
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

        Result<Unit> adequacyResult = CollateralAdequacyValidator.validateCollateralAdequacy(collaterals, context);
        if (adequacyResult.isFailure()) {
            return Result.failure(adequacyResult.err().orElseThrow());
        }

        Money totalNewCollateralAmount = collaterals.stream()
                .map(Collateral::usedAmount)
                .reduce(Money.zero(context.arrangement().getCurrencyType()).unwrap(), (a, b) -> a.add(b)
                        .unwrap());

        Result<Unit> valueValidationResult =
                CollateralAdequacyValidator.validateTotalCollateralValue(totalNewCollateralAmount, requiredAmount);
        if (valueValidationResult.isFailure()) {
            return Result.failure(valueValidationResult.err().orElseThrow());
        }

        return Result.success(CollateralData.initial(
                command.loanFacilityId(), command.uid(), command.collaterals(), command.version()));
    }

    private final AddFacilityCollateralCommandMapper mapper;
    private final AddFacilityCollateralDependencyLoader dependencyLoader;
    private final BranchAccessValidator branchAccessValidator;
    private final ReserveCollateralsStep reserveCollateralsStep;
    private final AddCollateralStep addCollateralStep;
}
