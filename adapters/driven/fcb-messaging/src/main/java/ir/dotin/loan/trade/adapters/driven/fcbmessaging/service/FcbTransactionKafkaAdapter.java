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
@Profile("kafka-fcb")
@RequiredArgsConstructor
public class FcbTransactionKafkaAdapter implements TransactionPostingPort {

    private final FcbKafkaClient kafkaClient;
    private final FcbKafkaProperties properties;
    private final Clock clock;

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
                    mappingResult.notification().getErrorMessages());
            return Result.failure(mappingResult.notification());
        }

        PostTransactionRequest request = mappingResult.orElseThrow();
        log.debug(
                "Successfully mapped LoanTransaction to IssueDocumentRequest - items: {}",
                request.getItems().size());

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.getTransactionTimeout());

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbKafkaBaseResponse raw = result.orElseThrow();
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
                    mergedTransactionResult.notification().getErrorMessages());
            return Result.failure(mergedTransactionResult.notification());
        }

        Result<TrackedTransactionNumber> postResult = postTransaction(mergedTransactionResult.orElseThrow());
        if (postResult.isFailure()) {
            return Result.failure(postResult.notification());
        }
        return Result.success(List.of(postResult.orElseThrow()));
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "reverseTransaction"})
    public Result<Void> reverseTransaction(TrackedTransactionNumber transactionNumber) {
        log.debug("Reversing transaction via Kafka - {}", transactionNumber.value());

        var request = ReverseTransactionRequest.builder()
                .transactionNumber(transactionNumber.value())
                .build();

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.getTransactionTimeout());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        return Result.success();
    }
}
