package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.commandhandler;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step.ApplyDisbursementStep;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step.PostTransactionsStep;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step.ResolveAccountsStep;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step.ValidateFacilityStep;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.disbursement.FacilityDependencyLoader;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class IrregularProgressiveDisbursementCommandHandlerTest {

    @Mock
    private WorkflowEngine engine;

    @Mock
    private FacilityDependencyLoader dependencyLoader;

    @Mock
    private BranchAccessValidator branchAccessValidator;

    @Mock
    private IrregularDisbursementSeedAssembler seedAssembler;

    @Mock
    private ValidateFacilityStep validateFacilityStep;

    @Mock
    private ResolveAccountsStep resolveAccountsStep;

    @Mock
    private PostTransactionsStep postTransactionsStep;

    @Mock
    private ApplyDisbursementStep applyDisbursementStep;

    private IrregularProgressiveDisbursementCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new IrregularProgressiveDisbursementCommandHandler(
                engine,
                dependencyLoader,
                branchAccessValidator,
                seedAssembler,
                validateFacilityStep,
                resolveAccountsStep,
                postTransactionsStep,
                applyDisbursementStep);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler.definition().workflowType()).isEqualTo("irregular-progressive-disbursement");
    }

    @Test
    void stepOrderAndIdsAreStable() {
        Workflow<IrregularDisbursementData> workflow = handler.definition();

        List<String> stepIds = workflow.steps().stream()
                .map(Step::stepId)
                .map(id -> id.value())
                .toList();

        assertThat(stepIds)
                .containsExactly("validate-facility", "resolve-accounts", "post-transactions", "apply-disbursement");
    }
}
