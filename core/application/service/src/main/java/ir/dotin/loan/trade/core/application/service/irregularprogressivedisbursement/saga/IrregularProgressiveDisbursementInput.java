package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.saga;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.saga.IrregularProgressiveDisbursementSagaData.InstallmentSpecData;

public record IrregularProgressiveDisbursementInput(
        UUID facilityId,
        String branchCode,
        TransactionConfig transactionConfig,
        @Nullable LocalDate disbursementDate,
        long expectedVersion,
        BigDecimal trancheAmount,
        String currencyCode,
        int trancheNumber,
        @Nullable List<InstallmentSpecData> customPlanSpecs,
        List<InstallmentSpecData> approvedPlanSpecs)
        implements SagaInput {}
