package ir.dotin.loan.trade.adapters.driven.fcbmessaging.adapter;

import java.time.Duration;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconStateResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReemitOutboxResponse;
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
 * <p>Reads are intentionally NOT {@code @Cacheable}: a fresh {@code eventUid} is minted by the transport client on
 * every call, so FCB bypasses its idempotency cache and answers with current state (INV-10). Caching the reconciliation
 * state would defeat the whole purpose of a probe.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcbReconStateAdapter implements FcbReconStatePort {

    private final FcbRequestReplyClient kafkaClient;

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
                ReconStateResponse.class,
                FcbReconStateAdapter::mapToReconLoanFileState);
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
                ReemitOutboxResponse.class,
                FcbReconStateAdapter::mapToReemitOutcome);
    }

    private static Result<ReconLoanFileState> mapToReconLoanFileState(ReconStateResponse response) {
        return Result.success(new ReconLoanFileState(
                response.isExists(),
                response.getFileStatus(),
                response.getManualId(),
                response.getLastModifiedEpochMs(),
                response.isReachable(),
                response.getOutboxRef()));
    }

    private static Result<ReconReemitOutcome> mapToReemitOutcome(ReemitOutboxResponse response) {
        String status = response.getStatus() == null ? "UNKNOWN" : response.getStatus();
        return Result.success(new ReconReemitOutcome(response.getReemittedCount(), status));
    }

    private <R extends FcbBaseResponse, T> Result<T> sendAndMap(
            FcbBaseRequest request, Class<R> responseType, Function<R, Result<T>> responseMapper) {

        Result<FcbBaseResponse> result = kafkaClient.sendAndReceive(request, reconStateTimeout);
        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        FcbBaseResponse raw = result.unwrap();
        if (!responseType.isInstance(raw)) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, request.getOperationName());
        }

        return responseMapper.apply(responseType.cast(raw));
    }
}
