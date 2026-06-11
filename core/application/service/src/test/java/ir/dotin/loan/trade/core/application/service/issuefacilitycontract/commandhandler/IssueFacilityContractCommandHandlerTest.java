package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractDependencyLoader;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.component.FacilityContractValidator;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step.OpenAccountsStep;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step.PostTransactionStep;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step.UpdateFacilityStateStep;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step.ValidateFacilityStep;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.FacilityContractValidation;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class IssueFacilityContractCommandHandlerTest {

    @Mock
    private WorkflowEngine engine;

    @Mock
    private FacilityContractDependencyLoader dependencyLoader;

    @Mock
    private FacilityContractValidator facilityValidator;

    @Mock
    private FacilityContractValidation facilityContractValidation;

    @Mock
    private ValidateFacilityStep validateFacilityStep;

    @Mock
    private OpenAccountsStep openAccountsStep;

    @Mock
    private PostTransactionStep postTransactionStep;

    @Mock
    private UpdateFacilityStateStep updateFacilityStateStep;

    private IssueFacilityContractCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new IssueFacilityContractCommandHandler(
                engine,
                dependencyLoader,
                facilityValidator,
                facilityContractValidation,
                validateFacilityStep,
                openAccountsStep,
                postTransactionStep,
                updateFacilityStateStep);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler.definition().workflowType()).isEqualTo("issue-facility-contract");
    }

    @Test
    void stepOrderAndIdsAreStable() {
        Workflow<ContractData> workflow = handler.definition();

        List<String> stepIds = workflow.steps().stream()
                .map(Step::stepId)
                .map(id -> id.value())
                .toList();

        assertThat(stepIds)
                .containsExactly("validate-facility", "open-accounts", "post-transaction", "update-facility-state");
    }
}
