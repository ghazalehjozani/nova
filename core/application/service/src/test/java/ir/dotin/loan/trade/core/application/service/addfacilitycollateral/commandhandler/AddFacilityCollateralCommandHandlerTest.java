package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.AddFacilityCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.step.AddCollateralStep;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.step.ReserveCollateralsStep;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.CollateralData;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class AddFacilityCollateralCommandHandlerTest {

    @Mock
    private WorkflowEngine engine;

    @Mock
    private AddFacilityCollateralCommandMapper mapper;

    @Mock
    private AddFacilityCollateralDependencyLoader dependencyLoader;

    @Mock
    private BranchAccessValidator branchAccessValidator;

    @Mock
    private ReserveCollateralsStep reserveCollateralsStep;

    @Mock
    private AddCollateralStep addCollateralStep;

    private AddFacilityCollateralCommandHandler handler() {
        return new AddFacilityCollateralCommandHandler(
                engine, mapper, dependencyLoader, branchAccessValidator, reserveCollateralsStep, addCollateralStep);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler().definition().workflowType()).isEqualTo("add-facility-collateral");
    }

    @Test
    void stepOrderAndIdsAreStable() {
        Workflow<CollateralData> workflow = handler().definition();

        List<String> stepIds = workflow.steps().stream()
                .map(Step::stepId)
                .map(id -> id.value())
                .toList();

        assertThat(stepIds).containsExactly("reserve-collaterals", "add-collateral");
    }
}
