package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga;

import java.util.UUID;

import ir.dotin.platform.saga.api.definition.SagaInput;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;

public record FullLoanFacilityLifecycleInput(
        OriginateLoanFacilityCommand originationCommand,
        String branchCode,
        TransactionConfig transactionConfig,
        DisbursementMethod disbursementMethod,
        UUID correlationId)
        implements SagaInput {

    public static FullLoanFacilityLifecycleInput of(
            OriginateLoanFacilityCommand originationCommand,
            String branchCode,
            TransactionConfig transactionConfig,
            DisbursementMethod disbursementMethod,
            UUID correlationId) {
        return new FullLoanFacilityLifecycleInput(
                originationCommand, branchCode, transactionConfig, disbursementMethod, correlationId);
    }
}
