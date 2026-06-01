package ir.dotin.loan.trade.adapters.driven.fcbmessaging.adapter;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.TransactionResultKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.PostTransactionRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReverseTransactionRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaTransactionMapper;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.LoanTransactionMerger;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcbTransactionKafkaAdapter implements TransactionPostingPort {

    private final FcbRequestReplyClient kafkaClient;
    private final Clock clock;

    /**
     * Per-call transaction timeout, bound from Consul KV {@code nova.fcb.kafka.transaction-timeout} (defaults to 60s).
     */
    @Value("${nova.fcb.kafka.transaction-timeout:60s}")
    private Duration transactionTimeout;

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "postTransaction"})
    public Result<TrackedTransactionNumber> postTransaction(LoanTransaction loanTransaction) {
        UUID trackingId = UUID.randomUUID();
        log.info(
                "Posting transaction via Kafka - facilityId: {}, trackingId: {}",
                loanTransaction.loanFacilityId().value(),
                trackingId);

        Result<PostTransactionRequest> mappingResult =
                KafkaTransactionMapper.mapToIssueDocumentRequest(loanTransaction, trackingId);
        if (mappingResult.isFailure()) {
            log.error(
                    "Failed to map LoanTransaction to IssueDocumentRequest: {}",
                    mappingResult.err().orElseThrow().notification().getErrorMessages());
            return Result.failure(mappingResult.err().orElseThrow());
        }

        PostTransactionRequest request = mappingResult.unwrap();
        log.debug(
                "Successfully mapped LoanTransaction to IssueDocumentRequest - items: {}",
                request.getItems().size());

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, transactionTimeout);

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        FcbKafkaBaseResponse raw = result.unwrap();
        if (!(raw instanceof TransactionResultKafkaResponse response)) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "issue-general-document"));
        }

        return KafkaTransactionMapper.mapToTrackedTransactionNumber(response, trackingId, clock);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "postTransactions"})
    public Result<List<TrackedTransactionNumber>> postTransactions(
            LoanFacilityId facilityId, String documentComment, List<LoanTransaction> transactionsToPost) {
        if (transactionsToPost == null || transactionsToPost.isEmpty()) {
            return Result.success(List.of());
        }

        log.info(
                "Batch posting {} transactions via Kafka (merged into single document) for facility: {}",
                transactionsToPost.size(),
                facilityId.value());

        Result<LoanTransaction> mergedTransactionResult =
                LoanTransactionMerger.merge(clock, facilityId, documentComment, transactionsToPost);

        if (mergedTransactionResult.isFailure()) {
            log.error(
                    "Failed to merge transactions: {}",
                    mergedTransactionResult.err().orElseThrow().notification().getErrorMessages());
            return Result.failure(mergedTransactionResult.err().orElseThrow());
        }

        Result<TrackedTransactionNumber> postResult = postTransaction(mergedTransactionResult.unwrap());
        if (postResult.isFailure()) {
            return Result.failure(postResult.err().orElseThrow());
        }
        return Result.success(List.of(postResult.unwrap()));
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "reverseTransaction"})
    public Result<Unit> reverseTransaction(TrackedTransactionNumber transactionNumber) {
        log.debug("Reversing transaction via Kafka - {}", transactionNumber.value());

        var request = ReverseTransactionRequest.builder()
                .transactionNumber(transactionNumber.value())
                .build();

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, transactionTimeout);
        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        return Result.success();
    }
}
