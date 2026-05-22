package ir.dotin.loan.trade.core.application.service.shared.account;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.core.concurrent.ParallelFanout;
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

    @WithSpan("account.resolve.fanout")
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

        List<Supplier<Result<TopicResult>>> tasks = topicsToProcess.stream()
                .map(topic -> (Supplier<Result<TopicResult>>) () -> {
                    Result<AccountId> inner =
                            accountServicePort.openAccount(topic, currencyCode).map(AccountInfo::id);
                    return Result.success(new TopicResult(topic, inner));
                })
                .toList();

        Result<List<TopicResult>> fan = ParallelFanout.allOf(tasks);
        if (fan.isFailure()) {
            return Result.failure(fan.err().orElseThrow());
        }

        Notification notification = Notification.create();
        for (TopicResult tr : fan.unwrap()) {
            if (tr.result().isFailure()) {
                notification.merge(tr.result().err().orElseThrow().notification());
            } else {
                AccountId accountId = tr.result().unwrap();
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
