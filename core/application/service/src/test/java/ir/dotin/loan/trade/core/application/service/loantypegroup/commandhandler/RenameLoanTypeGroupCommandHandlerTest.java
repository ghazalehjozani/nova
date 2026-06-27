package ir.dotin.loan.trade.core.application.service.loantypegroup.commandhandler;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.model.StepId;
import ir.dotin.loan.trade.core.application.service.loantypegroup.step.RenameLoanTypeGroupStep;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RenameLoanTypeGroupCommandHandlerTest {

    @Mock
    private RenameLoanTypeGroupStep step;

    private RenameLoanTypeGroupCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RenameLoanTypeGroupCommandHandler(step);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler.definition().workflowType()).isEqualTo("rename-loan-type-group");
    }

    @Test
    void hasSingleWriteStep() {
        List<String> stepIds = handler.definition().steps().stream()
                .map(Step::stepId)
                .map(StepId::value)
                .toList();

        assertThat(stepIds).containsExactly("rename-loan-type-group:write");
    }
}
