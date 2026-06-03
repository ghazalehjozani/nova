package ir.dotin.loan.trade.core.application.service.shared.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountResolutionService {

    private final AccountServicePort accountServicePort;

    @WithSpan("account.resolve.batch")
    public Result<ResolvedAccounts> resolveAccounts(
            Set<LoanTopic> requiredTopics, Map<RelationType<?>, AccountId> existingAccounts, String currencyCode) {

        Map<RelationType<?>, AccountId> resolved = new LinkedHashMap<>();
        if (existingAccounts != null) {
            resolved.putAll(existingAccounts);
        }

        Set<RelationType<?>> seenRelationTypes = new HashSet<>();
        List<LoanTopic> topicsToProcess = requiredTopics.stream()
                .filter(topic -> {
                    RelationType<?> type = topic.relationType();
                    if (resolved.containsKey(type) || !seenRelationTypes.add(type)) {
                        log.debug("Account already exists or is being opened for {}", type);
                        return false;
                    }
                    return true;
                })
                .toList();

        if (topicsToProcess.isEmpty()) {
            return Result.success(new ResolvedAccounts(resolved));
        }

        Result<List<AccountInfo>> batch = accountServicePort.openAccounts(topicsToProcess, currencyCode);
        if (batch.isFailure()) {
            return Result.failure(batch.err().orElseThrow());
        }

        List<AccountInfo> opened = batch.unwrap();
        for (int i = 0; i < topicsToProcess.size(); i++) {
            LoanTopic topic = topicsToProcess.get(i);
            AccountId accountId = opened.get(i).id();
            resolved.put(topic.relationType(), accountId);
            log.info("Opened account {} for {}", accountId.value(), topic.relationType());
        }

        return Result.success(new ResolvedAccounts(resolved));
    }

    public Result<List<AccountNumber>> closeAccounts(Collection<AccountNumber> accountNumbers) {
        if (accountNumbers == null || accountNumbers.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return accountServicePort.closeAccounts(new ArrayList<>(accountNumbers));
    }
}
