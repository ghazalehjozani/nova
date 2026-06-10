package ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.commandhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.saga.api.definition.SagaInput;
import ir.dotin.platform.pangaea.saga.api.handler.SagaCommandHandler;
import ir.dotin.platform.pangaea.saga.api.orchestration.SagaOrchestrator;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.saga.LumpSumDisbursementInput;
import ir.dotin.loan.trade.core.application.service.lumpsumdisbursement.saga.LumpSumDisbursementSagaData;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.application.service.shared.util.DocumentMetadataUtils;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Service
public class LumpSumDisbursementCommandHandler
        extends SagaCommandHandler<LumpSumDisbursementCommand, LumpSumDisbursementSagaData> {

    private static final Logger log = LoggerFactory.getLogger(LumpSumDisbursementCommandHandler.class);

    private final TradeLoanFacilityRepository tradeLoanFacilityRepository;
    private final BranchAccessValidator branchAccessValidator;

    public LumpSumDisbursementCommandHandler(
            SagaOrchestrator<LumpSumDisbursementSagaData> sagaOrchestrator,
            TradeLoanFacilityRepository tradeLoanFacilityRepository,
            BranchAccessValidator branchAccessValidator) {
        super(sagaOrchestrator);
        this.tradeLoanFacilityRepository = tradeLoanFacilityRepository;
        this.branchAccessValidator = branchAccessValidator;
    }

    @Override
    protected String sagaType() {
        return "lump-sum-disbursement";
    }

    @Override
    protected Result<SagaInput> prepare(LumpSumDisbursementCommand command) {
        log.info("Starting lump sum disbursement for facility: {}", command.loanFacilityId());

        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        return loadFacility(loanFacilityId)
                .flatMap(facility -> branchAccessValidator
                        .verifyCallerCoversFacility(command.branchCode(), facility)
                        .map(ignored -> facility))
                .flatMap(this::validateDisbursementMethod)
                .map(ignored -> buildInput(command));
    }

    private SagaInput buildInput(LumpSumDisbursementCommand command) {
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

        return LumpSumDisbursementInput.of(
                command.loanFacilityId(),
                command.branchCode(),
                transactionConfig,
                command.disbursementDate(),
                command.version());
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                tradeLoanFacilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<TradeLoanFacility> validateDisbursementMethod(TradeLoanFacility facility) {
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
}
