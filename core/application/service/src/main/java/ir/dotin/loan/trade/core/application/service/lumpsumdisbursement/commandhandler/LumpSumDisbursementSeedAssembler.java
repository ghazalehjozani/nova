package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.commandhandler;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.workflow.LumpSumData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Component
public class LumpSumDisbursementSeedAssembler {

    public Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.LUMP_SUM)
                .map(ignored -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod() != null
                                        ? sl.getDisbursementMethod().name()
                                        : "null")
                                .orElse("UNKNOWN"))));
    }

    public LumpSumData buildData(LumpSumDisbursementCommand command) {
        TransactionConfig transactionConfig = new TransactionConfig(
                DocumentMetadataUtils.orEmpty(command.terminalType()),
                DocumentMetadataUtils.orEmpty(command.terminalId()),
                DocumentMetadataUtils.orEmpty(command.terminalIp()),
                DocumentMetadataUtils.orEmpty(command.productCode()),
                command.userId(),
                DocumentMetadataUtils.orEmpty(command.toolSource()),
                DocumentMetadataUtils.orEmpty(command.networkType()),
                command.branchCode(),
                DocumentMetadataUtils.orEmpty(command.channel()));

        return LumpSumData.initial(
                command.loanFacilityId(),
                command.branchCode(),
                transactionConfig,
                command.disbursementDate(),
                command.version());
    }
}
