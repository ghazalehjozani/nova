package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.compensation.step;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.RunId;

final class WorkflowContextStub implements WorkflowContext<RevertCollateralData> {

    private final RunId runId = RunId.of(UUID.randomUUID());
    private RevertCollateralData data;

    WorkflowContextStub(RevertCollateralData data) {
        this.data = data;
    }

    @Override
    public RunId runId() {
        return runId;
    }

    @Override
    public String workflowType() {
        return "compensate-collateral";
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
    public RevertCollateralData data() {
        return data;
    }

    @Override
    public void updateData(Function<RevertCollateralData, RevertCollateralData> updater) {
        this.data = updater.apply(this.data);
    }
}
