package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import ir.dotin.platform.saga.api.definition.SagaInput;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;

public record FullLoanFacilityLifecycleInput(
        OriginateLoanFacilityCommand originationCommand,
        BigDecimal trancheAmount,
        String branchCode,
        TransactionConfig transactionConfig,
        DisbursementMethod disbursementMethod,
        LocalDate disbursementDate,
        UUID correlationId)
        implements SagaInput {

    public static FullLoanFacilityLifecycleInput of(
            OriginateLoanFacilityCommand originationCommand,
            BigDecimal trancheAmount,
            String branchCode,
            TransactionConfig transactionConfig,
            DisbursementMethod disbursementMethod,
            LocalDate disbursementDate,
            UUID correlationId) {
        return new FullLoanFacilityLifecycleInput(
                originationCommand,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId);
    }
}
