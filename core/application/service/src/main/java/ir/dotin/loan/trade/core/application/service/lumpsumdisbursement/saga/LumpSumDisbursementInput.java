package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.saga;

import java.time.LocalDate;
import java.util.UUID;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;

public record LumpSumDisbursementInput(
        UUID facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        LocalDate disbursementDate,
        long expectedVersion)
        implements SagaInput {

    public static LumpSumDisbursementInput of(
            UUID facilityId,
            String branchCode,
            TransactionConfig config,
            LocalDate disbursementDate,
            long expectedVersion) {
        return new LumpSumDisbursementInput(facilityId, branchCode, config, disbursementDate, expectedVersion);
    }
}
