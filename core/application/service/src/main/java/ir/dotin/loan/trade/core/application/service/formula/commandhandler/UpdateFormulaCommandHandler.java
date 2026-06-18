package ir.dotin.loan.trade.core.application.service.formula.commandhandler;

import java.time.Duration;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.cqrs.command.UpdateFormulaCommand;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.model.RetryPolicy;
import ir.dotin.loan.trade.core.application.service.formula.step.UpdateFormulaWriteStep;
import ir.dotin.loan.trade.core.application.service.formula.step.ValidateUpdateFormulaInFcbStep;
import ir.dotin.loan.trade.core.application.service.formula.workflow.UpdateFormulaData;
import ir.dotin.loan.trade.core.application.service.formula.workflow.UpdateFormulaStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.remote;
import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.write;

@Service
@RequiredArgsConstructor
public final class UpdateFormulaCommandHandler
        implements WorkflowCommandHandler<UpdateFormulaCommand, UpdateFormulaData> {

    @Override
    public Workflow<UpdateFormulaData> definition() {
        // @formatter:off
        return Workflow.<UpdateFormulaData>named("update-formula")
                .step(remote(UpdateFormulaStep.VALIDATE_IN_FCB, validateUpdateFormulaInFcbStep)
                        .retry(RetryPolicy.CONSERVATIVE)
                        .timeout(Duration.ofSeconds(30)))
                .step(write(UpdateFormulaStep.PERSIST, updateFormulaWriteStep))
                .build();
        // @formatter:on
    }

    @Override
    public Result<UpdateFormulaData> seed(UpdateFormulaCommand command) {
        return Result.success(new UpdateFormulaData(command));
    }

    private final ValidateUpdateFormulaInFcbStep validateUpdateFormulaInFcbStep;
    private final UpdateFormulaWriteStep updateFormulaWriteStep;
}
