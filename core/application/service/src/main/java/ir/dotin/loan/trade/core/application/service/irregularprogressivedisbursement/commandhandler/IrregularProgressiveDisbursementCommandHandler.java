package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.commandhandler;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.platform.pangaea.saga.api.handler.SagaCommandHandler;
import ir.dotin.platform.pangaea.saga.api.orchestration.SagaOrchestrator;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.InstallmentRecalculationService;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentSpec;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularProgressiveDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.InstallmentScheduleRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.mapper.IrregularProgressiveDisbursementInstallmentSchedulePlanMapper;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.saga.IrregularProgressiveDisbursementInput;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.saga.IrregularProgressiveDisbursementSagaData;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.saga.IrregularProgressiveDisbursementSagaData.InstallmentSpecData;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static java.util.Objects.requireNonNull;

@Service
public class IrregularProgressiveDisbursementCommandHandler
        extends SagaCommandHandler<IrregularProgressiveDisbursementCommand, IrregularProgressiveDisbursementSagaData> {

    private static final Logger log = LoggerFactory.getLogger(IrregularProgressiveDisbursementCommandHandler.class);

    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final InstallmentScheduleRepository installmentScheduleRepository;
    private final InstallmentRecalculationService recalculationService;
    private final IrregularProgressiveDisbursementInstallmentSchedulePlanMapper planMapper;
    private final BranchAccessValidator branchAccessValidator;

    public IrregularProgressiveDisbursementCommandHandler(
            SagaOrchestrator<IrregularProgressiveDisbursementSagaData> sagaOrchestrator,
            TradeLoanFacilityRepository tradeLoanFacilityRepository,
            InstallmentScheduleRepository installmentScheduleRepository,
            InstallmentRecalculationService recalculationService,
            IrregularProgressiveDisbursementInstallmentSchedulePlanMapper planMapper,
            BranchAccessValidator branchAccessValidator) {
        super(sagaOrchestrator);
        this.tradeLoanFacilityRepository = tradeLoanFacilityRepository;
        this.installmentScheduleRepository = installmentScheduleRepository;
        this.recalculationService = recalculationService;
        this.planMapper = planMapper;
        this.branchAccessValidator = branchAccessValidator;
    }

    @Override
    protected String sagaType() {
        return "irregular-progressive-disbursement";
    }

    @Override
    protected Result<SagaInput> prepare(IrregularProgressiveDisbursementCommand command) {
        log.info("Starting irregular disbursement for facility: {}", command.loanFacilityId());

        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return loadFacility(loanFacilityId)
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .flatMap(this::validateDisbursementMethod)
                .flatMap(facility -> loadInstallmentSchedule(facility)
                        .flatMap(schedule -> approvePlan(command, facility, schedule)));
    }

    private Result<SagaInput> approvePlan(
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
                .map(approvedInstallments -> buildInput(
                        command,
                        facility,
                        currencyType,
                        toSpecData(finalCustomPlan),
                        flattenInstallments(approvedInstallments)));
    }

    private SagaInput buildInput(
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

        int trancheNumber = facility.getSanctionedLoan().orElseThrow().getDisbursementCount() + 1;

        return new IrregularProgressiveDisbursementInput(
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

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<InstallmentSchedule> loadInstallmentSchedule(TradeLoanFacility facility) {
        return facility.getInstallmentScheduleId()
                .map(scheduleId -> Result.fromOptional(
                        installmentScheduleRepository.findById(scheduleId),
                        () -> FailureCause.notFound(Notification.ofError(
                                TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                                facility.getId().value()))))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INSTALLMENT_SCHEDULE_NOT_FOUND,
                        facility.getId().value())));
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
        return facility.getSanctionedLoan()
                .filter(sl -> sl.getDisbursementMethod() == DisbursementMethod.IRREGULAR_PROGRESSIVE)
                .map(ignored -> Result.success(facility))
                .orElseGet(() -> Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.INVALID_DISBURSEMENT_METHOD,
                        facility.getSanctionedLoan()
                                .map(sl -> sl.getDisbursementMethod() != null
                                        ? sl.getDisbursementMethod().name()
                                        : "null")
                                .orElse("UNKNOWN"))));
    }
}
