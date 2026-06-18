package ir.dotin.loan.trade.core.application.service.formula.commandhandler;

import java.time.Duration;

import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.service.cqrs.command.CreateFormulaCommand;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.model.RetryPolicy;
import ir.dotin.loan.trade.core.application.service.formula.step.PersistFormulaStep;
import ir.dotin.loan.trade.core.application.service.formula.step.ValidateCreateFormulaInFcbStep;
import ir.dotin.loan.trade.core.application.service.formula.workflow.CreateFormulaData;
import ir.dotin.loan.trade.core.application.service.formula.workflow.CreateFormulaStep;

import lombok.RequiredArgsConstructor;

import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.remote;
import static ir.dotin.platform.pangaea.workflow.api.definition.Steps.write;

@Service
@RequiredArgsConstructor
public final class CreateFormulaCommandHandler
        implements WorkflowCommandHandler<CreateFormulaCommand, CreateFormulaData> {

    @Override
    public Workflow<CreateFormulaData> definition() {
        // @formatter:off
        return Workflow.<CreateFormulaData>named("create-formula")
                .step(remote(CreateFormulaStep.VALIDATE_IN_FCB, validateCreateFormulaInFcbStep)
                        .retry(RetryPolicy.CONSERVATIVE)
                        .timeout(Duration.ofSeconds(30)))
                .step(write(CreateFormulaStep.PERSIST, persistFormulaStep))
                .build();
        // @formatter:on
    }

    @Override
    public Result<CreateFormulaData> seed(CreateFormulaCommand command) {
        return Result.success(new CreateFormulaData(command));
    }

    private final ValidateCreateFormulaInFcbStep validateCreateFormulaInFcbStep;
    private final PersistFormulaStep persistFormulaStep;
}
