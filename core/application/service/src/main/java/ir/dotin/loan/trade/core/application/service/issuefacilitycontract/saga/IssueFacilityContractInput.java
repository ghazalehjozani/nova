package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.saga;

import java.util.UUID;

import ir.dotin.platform.saga.api.definition.SagaInput;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;

public record IssueFacilityContractInput(UUID facilityId, String branchCode, TransactionConfig transactionConfig)
        implements SagaInput {

    public static IssueFacilityContractInput of(UUID facilityId, String branchCode, TransactionConfig config) {
        return new IssueFacilityContractInput(facilityId, branchCode, config);
    }
}
