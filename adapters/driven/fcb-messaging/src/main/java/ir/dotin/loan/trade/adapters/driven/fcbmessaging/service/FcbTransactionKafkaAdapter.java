package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReverseTransactionRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response.TransactionResultKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbKafkaLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaTransactionMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("kafka-fcb")
@RequiredArgsConstructor
public class FcbTransactionKafkaAdapter implements TransactionPostingPort {

    private final FcbKafkaClient kafkaClient;
    private final FcbKafkaProperties properties;
    private final Clock clock;

    @Override
    public Result<TrackedTransactionNumber> postTransaction(LoanTransaction loanTransaction) {
        UUID trackingId = UUID.randomUUID();
        log.info(
                "Posting transaction via Kafka - facilityId: {}, trackingId: {}",
                loanTransaction.loanFacilityId().value(),
                trackingId);

        var request = KafkaTransactionMapper.mapToPostTransactionRequest(loanTransaction, trackingId);

        Result<FcbKafkaBaseResponse> result =
                kafkaClient.sendAndReceive(request, properties.transactionTimeout());

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        FcbKafkaBaseResponse raw = result.orElseThrow();
        if (!(raw instanceof TransactionResultKafkaResponse response)) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "issue-general-document"));
        }

        return KafkaTransactionMapper.mapToTrackedTransactionNumber(response, trackingId, clock);
    }

    @Override
    public Result<List<TrackedTransactionNumber>> postTransactions(
            LoanFacilityId facilityId, String documentComment, List<LoanTransaction> transactionsToPost) {
        if (transactionsToPost == null || transactionsToPost.isEmpty()) {
            return Result.success(List.of());
        }

        log.info(
                "Batch posting {} transactions via Kafka for facility: {}",
                transactionsToPost.size(),
                facilityId.value());

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
        log.debug("Reversing transaction via Kafka - {}", transactionNumber.value());

        var request = new ReverseTransactionRequest(transactionNumber.value());

        Result<FcbKafkaBaseResponse> result =
                kafkaClient.sendAndReceive(request, properties.transactionTimeout());

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        return Result.success();
    }
}
