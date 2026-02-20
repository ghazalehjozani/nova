package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbMessagingProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsParameter;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbJmsLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.JmsTransactionMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("activemq")
public class FcbTransactionJmsAdapter implements TransactionPostingPort {

    private final FcbJmsClient jmsClient;
    private final JmsTemplate jmsTemplate;
    private final FcbMessagingProperties properties;
    private final Clock clock;

    public FcbTransactionJmsAdapter(
            FcbJmsClient jmsClient,
            @Qualifier("fcbTransactionJmsTemplate") JmsTemplate jmsTemplate,
            FcbMessagingProperties properties,
            Clock clock) {
        this.jmsClient = jmsClient;
        this.jmsTemplate = jmsTemplate;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public Result<TrackedTransactionNumber> postTransaction(LoanTransaction loanTransaction) {
        UUID trackingId = UUID.randomUUID();
        log.info(
                "Posting transaction via JMS - facilityId: {}, trackingId: {}",
                loanTransaction.loanFacilityId().value(),
                trackingId);

        List<FcbJmsParameter> parameters = JmsTransactionMapper.mapPostTransactionParams(loanTransaction, trackingId);

        FcbJmsRequest request = new FcbJmsRequest(trackingId.toString(), "issue-general-document", parameters, null);

        Result<FcbJmsResponse> result = jmsClient.sendAndReceive(jmsTemplate, request, properties.transaction());

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        FcbJmsResponse response = result.orElseThrow();
        if (response.payload() == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "issue-general-document"));
        }

        return JmsTransactionMapper.mapToTrackedTransactionNumber(response.payload(), trackingId, clock);
    }

    /**
     * Posts a batch of transactions to FCB individually.
     *
     * <p><b>Partial-failure risk:</b> Transactions are posted one by one with no compensation mechanism. If transaction
     * {@code N} fails, transactions {@code 0..N-1} are already committed in FCB and cannot be automatically rolled back
     * by this method. Callers that require all-or-nothing semantics must implement a Saga compensation pattern at the
     * application layer (e.g. call {@link #reverseTransaction} for each already-posted entry before propagating the
     * failure).
     */
    @Override
    public Result<List<TrackedTransactionNumber>> postTransactions(
            LoanFacilityId facilityId, String documentComment, List<LoanTransaction> transactionsToPost) {
        if (transactionsToPost == null || transactionsToPost.isEmpty()) {
            return Result.success(List.of());
        }

        log.info(
                "Batch posting {} transactions via JMS for facility: {}",
                transactionsToPost.size(),
                facilityId.value());

        // Post each transaction individually - merging is done at the application layer
        // if needed, or we delegate to single postTransaction
        // For now, post each and collect results
        java.util.List<TrackedTransactionNumber> results = new java.util.ArrayList<>();
        for (LoanTransaction transaction : transactionsToPost) {
            Result<TrackedTransactionNumber> postResult = postTransaction(transaction);
            if (postResult.isFailure()) {
                return Result.failure(postResult.notification());
            }
            results.add(postResult.orElseThrow());
        }
        return Result.success(results);
    }

    @Override
    public Result<Void> reverseTransaction(TrackedTransactionNumber transactionNumber) {
        log.debug("Reversing transaction via JMS - {}", transactionNumber.value());

        List<FcbJmsParameter> parameters = JmsTransactionMapper.mapReverseTransactionParams(transactionNumber);

        FcbJmsRequest request =
                new FcbJmsRequest(UUID.randomUUID().toString(), "cancel-transfer-money-loan", parameters, null);

        Result<FcbJmsResponse> result = jmsClient.sendAndReceive(jmsTemplate, request, properties.transaction());

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        FcbJmsResponse reverseResponse = result.orElseThrow();
        if (reverseResponse.payload() == null) {
            return Result.failure(Notification.ofError(
                    FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "cancel-transfer-money-loan"));
        }

        return JmsTransactionMapper.mapToReverseResult(reverseResponse.payload());
    }
}
