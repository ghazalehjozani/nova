package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSetMultimap;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.i18n.ValidationLocalizedMessageCodes;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.CalculationContext;
import ir.dotin.loan.baseloan.core.domain.shared.strategy.factory.ArticleComponentFactory;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumbers;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.ArticleComponent;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.trade.core.application.ports.driven.client.BankPort;
import ir.dotin.loan.trade.core.application.ports.driven.client.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.driven.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.i18n.IssueFacilityContractErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;
import ir.dotin.loan.trade.core.domain.loanfacility.enums.IssueContractBankCommitmentArticleType;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeIssueContractTransactionService;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class IssueFacilityContractCommandHandler implements CommandHandler<IssueFacilityContractCommand> {

    private static final Logger log = LoggerFactory.getLogger(IssueFacilityContractCommandHandler.class);

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final TradeLoanTypeRepository tradeLoanTypeRepository;
    private final TradeIssueContractTransactionService transactionService;
    private final TransactionPostingPort transactionPostingPort;
    private final IssueFacilityContractConfiguration configuration;
    private final BankPort bankPort;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(IssueFacilityContractCommand command) {

        return loadFacility(command)
                .flatMap((TradeLoanFacility facility) -> {
                    TradeLoanType tradeLoanType = tradeLoanTypeRepository
                            .findById(facility.getLoanTypeId())
                            .get();
                    return generateAndPostTransactions(facility, tradeLoanType);
                })
                .peekValue(result -> issueAndSaveContract(result.facility(), result.transactionNumbers()))
                .peekValue(ignored -> log.debug("Facility contract issued: {}", command.loanFacilityId()))
                .mapNonNull(result -> result.facility().domainEvents());
    }

    private Result<TradeLoanFacility> loadFacility(IssueFacilityContractCommand command) {
        return Result.fromOptional(
                loanFacilityRepository.findById(LoanFacilityId.of(command.loanFacilityId())),
                Notification.ofError(IssueFacilityContractErrorCodes.FACILITY_NOT_FOUND, command.loanFacilityId()));
    }

    private Result<ContractIssuanceResult> generateAndPostTransactions(
            TradeLoanFacility facility, TradeLoanType tradeLoanType) {
        return buildPostTitle(facility).flatMap(postTitle -> extractSanctionedLoan(facility)
                .flatMap(sanctionedLoan -> buildCalculationContext(facility, sanctionedLoan, tradeLoanType, postTitle))
                .flatMap(context -> calculateAndPostTransaction(context, postTitle))
                .mapNonNull(numbers -> new ContractIssuanceResult(facility, numbers)));
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

    private Result<CalculationContext<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType>>
            buildCalculationContext(
                    @NonNull TradeLoanFacility facility,
                    @NonNull TradeSanctionedLoan sanctionedLoan,
                    @NonNull TradeLoanType tradeLoanType,
                    @NonNull PostTitle postTitle) {

        if (sanctionedLoan.getCurrency() == null) {
            return Result.failure(Notification.ofError(
                    ValidationLocalizedMessageCodes.CALCULATION_CONTEXT_TRANSACTION_CURRENCY_REQUIRED));
        }

        if (requireNonNull(facility.getLoanApplication()).getBranch() == null) {
            return Result.failure(
                    Notification.ofError(ValidationLocalizedMessageCodes.CALCULATION_CONTEXT_BRANCH_CODE_REQUIRED));
        }

        Result<Map<IssueContractBankCommitmentArticleType, ArticleComponent>> articlesResult =
                buildArticleComponents(facility, sanctionedLoan, tradeLoanType);

        if (articlesResult.isFailure()) {
            return Result.failure(articlesResult.notification());
        }

        return CalculationContext
                .<TradeLoanFacility, TradeRelationType, IssueContractBankCommitmentArticleType>builder()
                .loanFacility(facility)
                .branchCode(bankPort.getCurrentBranchCode())
                .articleComponents(articlesResult.getValue())
                .postTitle(postTitle)
                .baseArticleMetadata(null)
                .build();
    }

    private Result<Map<IssueContractBankCommitmentArticleType, ArticleComponent>> buildArticleComponents(
            TradeLoanFacility facility, TradeSanctionedLoan sanctionedLoan, TradeLoanType tradeLoanType) {

        ImmutableSetMultimap<RelationType<TradeRelationType>, LoanTopic> relationTopics =
                tradeLoanType.getRelationTypeLoanTopics();
        Money approvedAmount = sanctionedLoan.getApprovedAmount();
        EconomicSector economicSector = facility.getLoanApplication().getEconomicSector();

        // Debit leg
        Result<ArticleComponent> debitLeg = ArticleComponentFactory.createWithLoanTopicLookup(
                relationTopics,
                IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG,
                economicSector,
                approvedAmount);
        if (debitLeg.isFailure()) {
            return Result.failure(debitLeg.notification());
        }

        // Credit leg
        Result<ArticleComponent> creditLeg = ArticleComponentFactory.createWithLoanTopicLookup(
                relationTopics,
                IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG,
                economicSector,
                approvedAmount);
        if (creditLeg.isFailure()) {
            return Result.failure(creditLeg.notification());
        }

        return Result.success(Map.of(
                IssueContractBankCommitmentArticleType.BANK_COMMITMENT_DEBIT_LEG, debitLeg.getValue(),
                IssueContractBankCommitmentArticleType.BANK_COMMITMENT_CREDIT_LEG, creditLeg.getValue()));
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
        loanFacilityRepository.save(facility);
    }

    private record ContractIssuanceResult(
            TradeLoanFacility facility, TrackedTransactionNumbers<TradeRelationType> transactionNumbers) {}
}
