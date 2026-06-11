package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.step;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.PostTitle;
import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.accounting.document.api.model.metadata.OperationalInfo;
import ir.dotin.platform.accounting.document.core.factory.DocumentMetadataFactory;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Compensable;
import ir.dotin.platform.pangaea.workflow.api.definition.RemoteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.workflow.ContractData;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.shared.document.enums.DocumentMetadataType;
import ir.dotin.loan.trade.core.domain.shared.document.transaction.TradeIssueContractTransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostTransactionStep implements RemoteActivity<ContractData>, Compensable<ContractData> {

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeIssueContractTransactionService transactionService;
    private final TransactionPostingPort transactionPostingPort;
    private final IssueFacilityContractConfiguration configuration;
    private final Clock clock;

    public StepResult<Void> execute(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();
        ResolvedAccounts resolvedAccounts = data.getResolvedAccounts();

        var transactionResult = loadFacility(LoanFacilityId.of(data.facilityId()))
                .flatMap(facility -> loadLoanType(facility).flatMap(loanType -> createPostTitle(facility)
                        .flatMap(postTitle -> createTransaction(
                                facility, loanType, postTitle, data.transactionConfig(), resolvedAccounts))));

        if (transactionResult.isFailure()) {
            return StepResult.failure(transactionResult.err().orElseThrow());
        }

        var transaction = transactionResult.unwrap();
        var result = transactionPostingPort.postTransaction(transaction);

        if (result.isFailure()) {
            return StepResult.failure(result.err().orElseThrow());
        }

        var trackedNumber = result.unwrap();
        ctx.updateData(d -> d.withPostedTransaction(
                trackedNumber.value(), trackedNumber.trackingId(), trackedNumber.status(), trackedNumber.createdAt()));

        log.info("Transaction posted: {}", trackedNumber.value());
        return new StepResult.Success<>(null);
    }

    public StepResult<Void> compensate(WorkflowContext<ContractData> ctx) {
        var data = ctx.data();
        log.warn("Reversing transaction: {}", data.postedTransactionNumber());

        var trackedNumber = TrackedTransactionNumber.create(
                Objects.requireNonNull(data.postedTransactionNumber(), "postedTransactionNumber"),
                Objects.requireNonNull(data.postedTrackingId(), "postedTrackingId"),
                TransactionStatus.POSTED,
                clock);

        return StepResult.fromResult(transactionPostingPort.reverseTransaction(trackedNumber));
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanTypeRepository.findById(facility.getLoanTypeId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value())));
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        return PostTitle.of(
                configuration.getPostTitleTemplate().formatted(facility.getId().value()));
    }

    private Result<LoanTransaction> createTransaction(
            TradeLoanFacility facility,
            TradeLoanType loanType,
            PostTitle postTitle,
            TransactionConfig config,
            ResolvedAccounts resolvedAccounts) {

        return DocumentMetadataFactory.builder()
                .terminal(DocumentMetadataFactory.TerminalConfig.of(
                        Objects.requireNonNull(config.terminalType(), "terminalType"),
                        Objects.requireNonNull(config.terminalId(), "terminalId"),
                        Objects.requireNonNull(config.terminalIp(), "terminalIp")))
                .product(DocumentMetadataFactory.ProductConfig.of(
                        Objects.requireNonNull(config.productCode(), "productCode"),
                        loanType.getCode().value(),
                        facility.getLoanApplication()
                                .getApplicationNumber()
                                .get()
                                .formattedApplicationNumber()))
                .party(DocumentMetadataFactory.PartyConfig.of(
                        facility.getLoanApplication().getApplicant().customerNumber(),
                        facility.getLoanApplication().getApplicant().name().fullName(),
                        List.of()))
                .tool(DocumentMetadataFactory.ToolConfig.of(
                        Objects.requireNonNull(config.userId(), "userId"),
                        Objects.requireNonNull(config.toolSource(), "toolSource")))
                .network(DocumentMetadataFactory.NetworkConfig.of(
                        Objects.requireNonNull(config.networkType(), "networkType"),
                        Objects.requireNonNull(config.channel(), "channel")))
                .metadataType(DocumentMetadataType.ISSUE_CONTRACT.code())
                .operational(OperationalInfo.builder().build())
                .build()
                .flatMap(metadata -> transactionService.createIssueContractTransaction(
                        facility,
                        loanType,
                        BranchCode.of(Objects.requireNonNull(config.branchCode(), "branchCode"))
                                .unwrap(),
                        postTitle,
                        metadata,
                        resolvedAccounts));
    }
}
