package ir.dotin.loan.trade.core.application.service.removeguarantor.commandhandler;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.loan.trade.core.application.service.removeguarantor.step.RemoveGuarantorStep;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RemoveGuarantorCommandHandlerTest {

    @Mock
    private BranchAccessValidator branchAccessValidator;

    @Mock
    private RemoveGuarantorStep removeGuarantorStep;

    private RemoveGuarantorCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RemoveGuarantorCommandHandler(branchAccessValidator, removeGuarantorStep);
    }

    @Test
    void workflowTypeIsStable() {
        assertThat(handler.definition().workflowType()).isEqualTo("remove-guarantor");
    }

    @Test
    void singleWriteStepIsStable() {
        Workflow<RemoveGuarantorCommandHandler.Data> workflow = handler.definition();

        List<String> stepIds = workflow.steps().stream()
                .map(Step::stepId)
                .map(id -> id.value())
                .toList();

        assertThat(stepIds).hasSize(1);
    }
}
