package ir.dotin.loan.trade.core.application.service.shared.disbursement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.application.service.shared.account.AccountResolutionService;
import ir.dotin.loan.trade.core.application.service.shared.account.LoanTopicResolver;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.document.strategy.DisbursementStrategyProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountResolutionSupport {

    private final DisbursementStrategyProvider strategyProvider;
    private final LoanTopicResolver loanTopicResolver;
    private final AccountResolutionService accountResolutionService;

    public Result<Map<String, String>> resolveAccounts(
            TradeLoanFacility facility, TradeLoanType loanType, String currencyCode) {

        Set<TradeRelationType> requiredRelationTypes = strategyProvider.getAllRequiredRelationTypes(facility);

        Set<LoanTopic> requiredTopics = loanTopicResolver.resolveTopics(
                loanType, facility.getLoanApplication().getEconomicSector(), requiredRelationTypes);

        return accountResolutionService
                .resolveAccounts(requiredTopics, facility.getAccountInfoMap(), currencyCode)
                .map(this::serialize);
    }

    public void closeAccounts(@Nullable Map<String, String> openedAccounts) {
        if (openedAccounts == null || openedAccounts.isEmpty()) {
            return;
        }
        List<AccountNumber> accountNumbers = new ArrayList<>();
        for (String accountValue : openedAccounts.values()) {
            Result<AccountNumber> accountNumberResult = AccountNumber.of(accountValue);
            if (accountNumberResult.isFailure()) {
                log.warn("Skipping un-parsable account during compensation: {}", accountValue);
            } else {
                accountNumbers.add(accountNumberResult.unwrap());
            }
        }
        if (!accountNumbers.isEmpty()) {
            Result<List<AccountNumber>> closeResult = accountResolutionService.closeAccounts(accountNumbers);
            if (closeResult.isFailure()) {
                log.warn(
                        "Compensation close failed for {}: {}",
                        accountNumbers,
                        closeResult.err().orElseThrow());
            }
        }
    }

    private Map<String, String> serialize(ResolvedAccounts resolved) {
        return resolved.accountsByRelationType().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(), e -> e.getValue().value()));
    }
}
