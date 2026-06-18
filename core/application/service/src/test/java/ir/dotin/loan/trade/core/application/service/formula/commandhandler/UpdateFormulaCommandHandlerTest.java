package ir.dotin.loan.trade.core.application.service.formula.commandhandler;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.service.formula.step.UpdateFormulaWriteStep;
import ir.dotin.loan.trade.core.application.service.formula.step.ValidateUpdateFormulaInFcbStep;
import ir.dotin.loan.trade.core.application.service.formula.workflow.UpdateFormulaData;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class UpdateFormulaCommandHandlerTest {

    @Mock
    private ValidateUpdateFormulaInFcbStep validateUpdateFormulaInFcbStep;

    @Mock
    private UpdateFormulaWriteStep updateFormulaWriteStep;

    private UpdateFormulaCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateFormulaCommandHandler(validateUpdateFormulaInFcbStep, updateFormulaWriteStep);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler.definition().workflowType()).isEqualTo("update-formula");
    }

    @Test
    void stepOrderAndIdsAreStable() {
        Workflow<UpdateFormulaData> workflow = handler.definition();

        List<String> stepIds = workflow.steps().stream()
                .map(Step::stepId)
                .map(id -> id.value())
                .toList();

        assertThat(stepIds).containsExactly("validate-in-fcb", "persist");
    }
}
