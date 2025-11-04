package ir.dotin.loan.trade.core.application.service.interaction;

import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.interaction.OpenAccountClient;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAccountService implements OpenAccountClient {

    private final AccountServicePort accountServicePort;

    @Override
    public @NonNull Result<AccountInfo> interact(
            @NonNull LoanTopic input, Map<RelationType<?>, AccountId> accountCache) {
        Optional<TradeRelationType> relationType = extractRelationType(input);

        if (relationType.isPresent() && accountCache != null) {
            AccountId cachedAccountId = accountCache.get(relationType.get());
            if (cachedAccountId != null) {
                log.debug("Using cached account {} for relation type {}", cachedAccountId.value(), relationType.get());
                return Result.success(new AccountInfo(cachedAccountId, input));
            }
        }

        return accountServicePort.openAccount(input);
    }

    private Optional<TradeRelationType> extractRelationType(LoanTopic loanTopic) {
        return Optional.of((TradeRelationType) loanTopic.relationType());
    }
}
