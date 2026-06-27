package ir.dotin.loan.trade.core.application.service.loantypegroup.commandhandler;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.model.StepId;
import ir.dotin.loan.trade.core.application.service.loantypegroup.step.CreateLoanTypeGroupStep;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class CreateLoanTypeGroupCommandHandlerTest {

    @Mock
    private CreateLoanTypeGroupStep step;

    private CreateLoanTypeGroupCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateLoanTypeGroupCommandHandler(step);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler.definition().workflowType()).isEqualTo("create-loan-type-group");
    }

    @Test
    void hasSingleWriteStep() {
        List<String> stepIds = handler.definition().steps().stream()
                .map(Step::stepId)
                .map(StepId::value)
                .toList();

        assertThat(stepIds).containsExactly("create-loan-type-group:write");
    }
}
