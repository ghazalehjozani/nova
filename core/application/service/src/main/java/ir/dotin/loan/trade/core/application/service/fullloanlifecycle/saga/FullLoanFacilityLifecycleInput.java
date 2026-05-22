package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;

public record FullLoanFacilityLifecycleInput(
        OriginateLoanFacilityCommand originationCommand,
        List<FullLoanFacilityLifecycleCommand.CollateralDto> collaterals,
        BigDecimal trancheAmount,
        String branchCode,
        TransactionConfig transactionConfig,
        DisbursementMethod disbursementMethod,
        LocalDate disbursementDate,
        UUID correlationId,
        String confirmType)
        implements SagaInput {

    public static FullLoanFacilityLifecycleInput of(
            OriginateLoanFacilityCommand originationCommand,
            List<FullLoanFacilityLifecycleCommand.CollateralDto> collaterals,
            BigDecimal trancheAmount,
            String branchCode,
            TransactionConfig transactionConfig,
            DisbursementMethod disbursementMethod,
            LocalDate disbursementDate,
            UUID correlationId,
            String confirmType) {
        return new FullLoanFacilityLifecycleInput(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType);
    }
}
