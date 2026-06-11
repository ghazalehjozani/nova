package ir.dotin.loan.trade.core.application.service.loanfacilityrestructuring.step;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.RunId;

final class WorkflowContextStub<D> implements WorkflowContext<D> {

    private final RunId runId = RunId.of(UUID.randomUUID());
    private final String workflowType;
    private D data;

    WorkflowContextStub(String workflowType, D data) {
        this.workflowType = workflowType;
        this.data = data;
    }

    @Override
    public RunId runId() {
        return runId;
    }

    @Override
    public String workflowType() {
        return workflowType;
    }

    @Override
    public @Nullable String correlationId() {
        return null;
    }

    @Override
    public @Nullable String causationId() {
        return null;
    }

    @Override
    public Instant startedAt() {
        return Instant.EPOCH;
    }

    @Override
    public D data() {
        return data;
    }

    @Override
    public void updateData(Function<D, D> updater) {
        this.data = updater.apply(this.data);
    }
}
