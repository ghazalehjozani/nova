package ir.dotin.loan.trade.core.application.service.issuefacilitycontract.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.PostTitle;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.BankPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.configuration.IssueFacilityContractConfiguration;
import ir.dotin.loan.trade.core.application.service.issuefacilitycontract.i18n.IssueFacilityContractErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeIssueContractTransactionService;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueFacilityContractCommandHandler implements CommandHandler<IssueFacilityContractCommand> {

    private static final Logger log = LoggerFactory.getLogger(IssueFacilityContractCommandHandler.class);

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeIssueContractTransactionService transactionService;
    private final TransactionPostingPort transactionPostingPort;
    private final IssueFacilityContractConfiguration configuration;
    private final BankPort bankPort;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(IssueFacilityContractCommand command) {

        return loadFacility(command.loanFacilityId()).flatMap(facility -> loadLoanType(facility)
                .flatMap(loanType -> createPostTitle(facility)
                        .flatMap(postTitle -> createTransaction(facility, loanType, postTitle)))
                .flatMap(transactionPostingPort::postTransaction)
                .mapNonNull(transactionNumbers -> {
                    facility.issueContract(transactionNumbers, clock);
                    facilityRepository.save(facility);
                    log.info(
                            "Contract issued for facility: {}", facility.getId().value());
                    return facility;
                })
                .mapNonNull(AbstractAggregateRoot::domainEvents));
    }

    private Result<TradeLoanFacility> loadFacility(UUID facilityId) {
        return Result.fromOptional(
                facilityRepository.findById(LoanFacilityId.of(facilityId)),
                Notification.ofError(IssueFacilityContractErrorCodes.FACILITY_NOT_FOUND, facilityId));
    }

    private Result<TradeLoanType> loadLoanType(TradeLoanFacility facility) {
        return Result.fromOptional(
                loanTypeRepository.findById(facility.getLoanTypeId()),
                Notification.ofError(
                        IssueFacilityContractErrorCodes.LOAN_TYPE_NOT_FOUND,
                        facility.getLoanTypeId(),
                        facility.getId().value()));
    }

    private Result<PostTitle> createPostTitle(TradeLoanFacility facility) {
        return PostTitle.of(
                configuration.postTitleTemplate().formatted(facility.getId().value()));
    }

    private Result<LoanTransaction> createTransaction(
            TradeLoanFacility facility, TradeLoanType loanType, PostTitle postTitle) {

        return transactionService.createIssueContractTransaction(
                facility, loanType, bankPort.getCurrentBranchCode(), postTitle);
    }
}
