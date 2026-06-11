package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.model.RunId;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;

final class WorkflowContextStub implements WorkflowContext<ContractData> {

    private final RunId runId = RunId.of(UUID.randomUUID());
    private ContractData data;

    WorkflowContextStub(ContractData data) {
        this.data = data;
    }

    @Override
    public RunId runId() {
        return runId;
    }

    @Override
    public String workflowType() {
        return "issue-facility-contract";
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
    public ContractData data() {
        return data;
    }

    @Override
    public void updateData(Function<ContractData, ContractData> updater) {
        this.data = updater.apply(this.data);
    }
}
