package ir.dotin.loan.trade.core.application.service.assignloantypegroup.commandhandler;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.model.StepId;
import ir.dotin.loan.trade.core.application.service.assignloantypegroup.step.AssignLoanTypeToGroupStep;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class AssignLoanTypeToGroupCommandHandlerTest {

    @Mock
    private AssignLoanTypeToGroupStep step;

    private AssignLoanTypeToGroupCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AssignLoanTypeToGroupCommandHandler(step);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler.definition().workflowType()).isEqualTo("assign-loan-type-to-group");
    }

    @Test
    void hasSingleWriteStep() {
        List<String> stepIds = handler.definition().steps().stream()
                .map(Step::stepId)
                .map(StepId::value)
                .toList();

        assertThat(stepIds).containsExactly("assign-loan-type-to-group:write");
    }
}
