package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.step;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.RunId;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;

final class WorkflowContextStub implements WorkflowContext<LumpSumData> {

    private final RunId runId = RunId.of(UUID.randomUUID());
    private LumpSumData data;

    WorkflowContextStub(LumpSumData data) {
        this.data = data;
    }

    @Override
    public RunId runId() {
        return runId;
    }

    @Override
    public String workflowType() {
        return "lump-sum-disbursement";
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
    public LumpSumData data() {
        return data;
    }

    @Override
    public void updateData(Function<LumpSumData, LumpSumData> updater) {
        this.data = updater.apply(this.data);
    }
}
