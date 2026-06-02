package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.util.UUID;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;

public record IssueFacilityContractInput(
        UUID facilityId, String branchCode, TransactionConfig transactionConfig, long expectedVersion)
        implements SagaInput {

    public static IssueFacilityContractInput of(
            UUID facilityId, String branchCode, TransactionConfig config, long expectedVersion) {
        return new IssueFacilityContractInput(facilityId, branchCode, config, expectedVersion);
    }
}
