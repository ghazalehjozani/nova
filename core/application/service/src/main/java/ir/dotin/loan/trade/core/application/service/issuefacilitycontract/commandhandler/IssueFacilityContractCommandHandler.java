package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumbers;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.trade.core.application.ports.driven.client.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.driven.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.i18n.IssueFacilityContractErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeIssueContractTransactionService;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueFacilityContractCommandHandler implements CommandHandler<IssueFacilityContractCommand> {

    private static final Logger log = LoggerFactory.getLogger(IssueFacilityContractCommandHandler.class);

    private final TradeLoanFacilityRepository repository;
    private final TradeIssueContractTransactionService transactionService;
    private final TransactionPostingPort transactionPostingPort;
    private final IssueFacilityContractConfiguration configuration;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(IssueFacilityContractCommand command) {
        return loadFacility(command)
                .flatMap(this::generateAndPostTransactions)
                .peekValue(result -> issueAndSaveContract(result.facility(), result.transactionNumbers()))
                .peekValue(ignored -> log.debug("Facility contract issued: {}", command.loanFacilityId()))
                .mapNonNull(result -> result.facility().domainEvents());
    }

    private Result<TradeLoanFacility> loadFacility(IssueFacilityContractCommand command) {
        return Result.fromOptional(
                repository.findById(LoanFacilityId.of(command.loanFacilityId())),
                Notification.ofError(IssueFacilityContractErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()));
    }

    private Result<ContractIssuanceResult> generateAndPostTransactions(TradeLoanFacility facility) {
        return buildPostTitle(facility).flatMap(postTitle -> extractSanctionedLoan(facility)
                .flatMap(sanctionedLoan -> buildArticleComponents(sanctionedLoan)
                        .flatMap(components -> buildCalculationContext(facility, sanctionedLoan, postTitle, components))
                        .flatMap(context -> calculateAndPostTransaction(context, postTitle))
                        .mapNonNull(numbers -> new ContractIssuanceResult(facility, numbers))));
    }

    private Result<PostTitle> buildPostTitle(TradeLoanFacility facility) {
        String title =
                configuration.postTitleTemplate().formatted(facility.getId().value());
        return PostTitle.of(title);
    }

    private Result<TradeSanctionedLoan> extractSanctionedLoan(TradeLoanFacility facility) {
        return Result.fromOptional(
                facility.getSanctionedLoan(),
                Notification.ofError(
                        IssueFacilityContractErrorCodes.SANCTIONED_LOAN_NOT_FOUND,
                        facility.getId().value()));
    }

    private Result<Map<IssueContractBankCommitmentArticleType, ArticleComponent>> buildArticleComponents(
            TradeSanctionedLoan sanctionedLoan) {

        if (sanctionedLoan.getApprovedAmount() == null) {
            return Result.failure(Notification.ofError(IssueFacilityContractErrorCodes.APPROVED_AMOUNT_NOT_FOUND));
        }

        return ArticleComponent.of(sanctionedLoan.getApprovedAmount(), null)
                .mapNonNull(component -> Map.of(
                        IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG, component,
                        IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG, component));
    }

    private Result<CalculationContext<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType>>
            buildCalculationContext(
                    TradeLoanFacility facility,
                    TradeSanctionedLoan sanctionedLoan,
                    PostTitle postTitle,
                    Map<IssueContractBankCommitmentArticleType, ArticleComponent> components) {

        if (sanctionedLoan.getCurrency() == null) {
            return Result.failure(Notification.ofError(IssueFacilityContractErrorCodes.CURRENCY_NOT_FOUND));
        }

        if (facility.getLoanApplication().getBranch() == null) {
            return Result.failure(Notification.ofError(IssueFacilityContractErrorCodes.BRANCH_NOT_FOUND));
        }

        return CalculationContext.of(
                sanctionedLoan.getCurrency(),
                facility,
                facility.getLoanApplication().getBranch().code(),
                components,
                postTitle,
                null);
    }

    private Result<TrackedTransactionNumbers<TradeRelationType>> calculateAndPostTransaction(
            CalculationContext<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType> context,
            PostTitle postTitle) {

        return transactionService
                .calculateTransaction(context, postTitle.value())
                .flatMap(transactionPostingPort::postTransaction);
    }

    private void issueAndSaveContract(
            TradeLoanFacility facility, TrackedTransactionNumbers<TradeRelationType> transactionNumbers) {

        facility.issueContract(transactionNumbers, clock);
        repository.save(facility);
    }

    private record ContractIssuanceResult(
            TradeLoanFacility facility, TrackedTransactionNumbers<TradeRelationType> transactionNumbers) {}
}
