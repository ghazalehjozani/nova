package ir.dotin.loan.trade.core.application.service.shared.account;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ResolvedAccounts;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountResolutionService {

    private final AccountServicePort accountServicePort;

    public Result<ResolvedAccounts> resolveAccounts(
            Set<LoanTopic> requiredTopics, Map<RelationType<?>, AccountId> existingAccounts, String currencyCode) {

        Map<RelationType<?>, AccountId> resolved = new LinkedHashMap<>();
        if (existingAccounts != null) {
            resolved.putAll(existingAccounts);
        }

        Notification notification = Notification.create();

        for (LoanTopic topic : requiredTopics) {
            RelationType<?> relationType = topic.relationType();

            if (resolved.containsKey(relationType)) {
                log.debug("Account already exists for {}", relationType);
                continue;
            }

            Result<AccountId> result =
                    accountServicePort.openAccount(topic, currencyCode).map(AccountInfo::id);

            if (result.hasErrors()) {
                notification.merge(result.notification());
            } else {
                resolved.put(relationType, result.orElseThrow());
                log.info("Opened account {} for {}", result.orElseThrow().value(), relationType);
            }
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        return Result.success(new ResolvedAccounts(resolved));
    }
}
