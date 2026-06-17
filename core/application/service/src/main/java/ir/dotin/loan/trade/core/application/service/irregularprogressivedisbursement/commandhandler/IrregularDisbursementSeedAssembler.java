package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.commandhandler;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.InstallmentRecalculationService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularProgressiveDisbursementCommand;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.mapper.IrregularProgressiveDisbursementInstallmentSchedulePlanMapper;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData.InstallmentSpecData;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class IrregularDisbursementSeedAssembler {

    private final InstallmentRecalculationService recalculationService;
    private final IrregularProgressiveDisbursementInstallmentSchedulePlanMapper planMapper;

    public Result<IrregularDisbursementData> approvePlan(
            IrregularProgressiveDisbursementCommand command, TradeLoanFacility facility, InstallmentSchedule schedule) {

        CurrencyType currencyType =
                requireNonNull(facility.getSanctionedLoan().orElseThrow().getCurrency());
        Money trancheAmount =
                Money.valueOf(command.trancheAmount(), currencyType).unwrap();

        List<InstallmentSpec> customPlan = null;
        if (command.installmentSchedulePlan() != null) {
            customPlan = planMapper.mapSpecs(command.installmentSchedulePlan().installments(), currencyType);
        }
        List<InstallmentSpec> finalCustomPlan = customPlan;

        return recalculationService
                .recalculateForIrregularDisbursement(schedule, facility, trancheAmount, finalCustomPlan)
                .map(approvedInstallments -> buildData(
                        command,
                        facility,
                        currencyType,
                        toSpecData(finalCustomPlan),
                        flattenInstallments(approvedInstallments)));
    }

    private IrregularDisbursementData buildData(
            IrregularProgressiveDisbursementCommand command,
            TradeLoanFacility facility,
            CurrencyType currencyType,
            @Nullable List<InstallmentSpecData> customPlanSpecs,
            List<InstallmentSpecData> approvedPlanSpecs) {

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

        int trancheNumber = facility.getDisbursementCount() + 1;

        return IrregularDisbursementData.initial(
                command.loanFacilityId(),
                command.branchCode(),
                transactionConfig,
                command.disbursementDate(),
                command.version(),
                command.trancheAmount(),
                currencyType.getCode(),
                trancheNumber,
                customPlanSpecs,
                approvedPlanSpecs);
    }

    private @Nullable List<InstallmentSpecData> toSpecData(@Nullable List<InstallmentSpec> specs) {
        if (specs == null) {
            return null;
        }
        return specs.stream()
                .map(spec -> new InstallmentSpecData(
                        spec.sequenceNumber(),
                        spec.dueDate(),
                        spec.principalAmount().value(),
                        spec.interestAmount().value()))
                .toList();
    }

    private List<InstallmentSpecData> flattenInstallments(List<Installment> installments) {
        return installments.stream()
                .map(installment -> new InstallmentSpecData(
                        installment.getSequenceNumber(),
                        installment.getDueDate(),
                        installment.getScheduledAmount().principalAmount().value(),
                        installment.getScheduledAmount().interestAmount().value()))
                .toList();
    }
}
