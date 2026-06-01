package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.time.Duration;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconStateKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReemitOutboxKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReconStateRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReemitOutboxRequest;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbReconStatePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconReemitOutcome;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * FCB outbound adapter for the Nova↔FCB reconciliation corridor. Implements {@link FcbReconStatePort} over the same
 * Kafka request/reply client as the other FCB adapters.
 *
 * <p>Reads are intentionally NOT {@code @Cacheable}: a fresh {@code eventUid} is minted by {@link FcbKafkaClient} on
 * every call, so FCB bypasses its idempotency cache and answers with current state (INV-10). Caching the reconciliation
 * state would defeat the whole purpose of a probe.
 */
@Slf4j
@Service
@Profile("kafka-fcb")
@RequiredArgsConstructor
public class FcbReconStateKafkaAdapter implements FcbReconStatePort {

    private final FcbKafkaClient kafkaClient;

    /**
     * Per-call timeout for the recon-state / re-emit request-reply, bound from Consul KV
     * {@code reconciliation.recon-state-timeout} (defaults to 10s). Field-injected via {@code @Value} because this
     * adapter module cannot see the container-side reconciliation properties class (hexagonal module direction).
     */
    @Value("${reconciliation.recon-state-timeout:10s}")
    private Duration reconStateTimeout;

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadReconState"})
    public Result<ReconLoanFileState> loadReconState(String facilityId) {
        return sendAndMap(
                ReconStateRequest.builder().facilityId(facilityId).build(),
                ReconStateKafkaResponse.class,
                FcbReconStateKafkaAdapter::mapToReconLoanFileState);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "reemitOutbox"})
    public Result<ReconReemitOutcome> reemitOutbox(String facilityId, @Nullable String outboxRef) {
        return sendAndMap(
                ReemitOutboxRequest.builder()
                        .facilityId(facilityId)
                        .outboxRef(outboxRef)
                        .build(),
                ReemitOutboxKafkaResponse.class,
                FcbReconStateKafkaAdapter::mapToReemitOutcome);
    }

    private static Result<ReconLoanFileState> mapToReconLoanFileState(ReconStateKafkaResponse response) {
        return Result.success(new ReconLoanFileState(
                response.isExists(),
                response.getFileStatus(),
                response.getManualId(),
                response.getLastModifiedEpochMs(),
                response.isReachable(),
                response.getOutboxRef()));
    }

    private static Result<ReconReemitOutcome> mapToReemitOutcome(ReemitOutboxKafkaResponse response) {
        String status = response.getStatus() == null ? "UNKNOWN" : response.getStatus();
        return Result.success(new ReconReemitOutcome(response.getReemittedCount(), status));
    }

    private <R extends FcbKafkaBaseResponse, T> Result<T> sendAndMap(
            FcbKafkaBaseRequest request, Class<R> responseType, Function<R, Result<T>> responseMapper) {

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, reconStateTimeout);
        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        FcbKafkaBaseResponse raw = result.unwrap();
        if (!responseType.isInstance(raw)) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, request.getOperationName());
        }

        return responseMapper.apply(responseType.cast(raw));
    }
}
