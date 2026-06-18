package ir.dotin.loan.trade.core.application.service.formula.commandhandler;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.service.formula.step.DeleteFormulaWriteStep;
import ir.dotin.loan.trade.core.application.service.formula.workflow.DeleteFormulaData;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class DeleteFormulaCommandHandlerTest {

    @Mock
    private DeleteFormulaWriteStep deleteFormulaWriteStep;

    private DeleteFormulaCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeleteFormulaCommandHandler(deleteFormulaWriteStep);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler.definition().workflowType()).isEqualTo("delete-formula");
    }

    @Test
    void singleWriteStepIsStable() {
        Workflow<DeleteFormulaData> workflow = handler.definition();

        List<String> stepIds = workflow.steps().stream()
                .map(Step::stepId)
                .map(id -> id.value())
                .toList();

        assertThat(stepIds).hasSize(1);
    }
}
