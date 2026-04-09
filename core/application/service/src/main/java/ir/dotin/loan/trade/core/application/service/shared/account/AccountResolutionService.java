package ir.dotin.loan.trade.core.application.service.shared.account;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountResolutionService {

    private final AccountServicePort accountServicePort;

    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

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

        List<CompletableFuture<TopicResult>> futures = topicsToProcess.stream()
                .map(topic -> CompletableFuture.supplyAsync(
                        () -> new TopicResult(
                                topic,
                                accountServicePort
                                        .openAccount(topic, currencyCode)
                                        .map(AccountInfo::id)),
                        executor))
                .toList();

        Notification notification = Notification.create();

        for (var future : futures) {
            TopicResult tr = future.join();
            if (tr.result().hasErrors()) {
                notification.merge(tr.result().notification());
            } else {
                AccountId accountId = tr.result().orElseThrow();
                resolved.put(tr.topic().relationType(), accountId);
                log.info(
                        "Opened account {} for {}",
                        accountId.value(),
                        tr.topic().relationType());
            }
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        return Result.success(new ResolvedAccounts(resolved));
    }

    record TopicResult(LoanTopic topic, Result<AccountId> result) {}
}
