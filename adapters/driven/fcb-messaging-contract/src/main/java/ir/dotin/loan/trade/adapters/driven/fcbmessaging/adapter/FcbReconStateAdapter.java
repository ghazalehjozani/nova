package ir.dotin.loan.trade.adapters.driven.fcbmessaging.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbReconStateProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconPeerSignal;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconStateResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReemitOutboxResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReconStateRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReemitOutboxRequest;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.EventPeerSignal;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FcbOutboxReemitPort;
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
public class FcbReconStateAdapter implements FcbReconStatePort, FcbOutboxReemitPort {

    private final FcbRequestReplyClient kafkaClient;
    private final FcbReconStateProperties properties;

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadReconState"})
    public Result<ReconLoanFileState> loadReconState(String facilityId, @Nullable List<String> forwardEventUids) {
        List<String> uids =
                forwardEventUids == null || forwardEventUids.isEmpty() ? null : List.copyOf(forwardEventUids);
        return sendAndMap(
                ReconStateRequest.builder()
                        .facilityId(facilityId)
                        .forwardEventUids(uids)
                        .build(),
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
                response.getOutboxRef(),
                mapPeerSignals(response.getPeerSignals()),
                response.isDltPresentForFacility()));
    }

    private static List<EventPeerSignal> mapPeerSignals(@Nullable List<ReconPeerSignal> wireSignals) {
        if (wireSignals == null || wireSignals.isEmpty()) {
            return List.of();
        }
        List<EventPeerSignal> mapped = new ArrayList<>(wireSignals.size());
        for (ReconPeerSignal wire : wireSignals) {
            String eventUid = wire.getEventUid();
            if (eventUid == null || eventUid.isBlank()) {
                // A signal without a join key cannot be matched to a forward outbox row — drop it (degrades that uid to
                // "no signal" → FCB_LAG, safe), but surface the malformed reply rather than silently swallowing it.
                log.warn("FCB recon-state returned a peer signal with no eventUid — dropping it");
                continue;
            }
            mapped.add(new EventPeerSignal(
                    eventUid,
                    parseIdempotencyState(wire.getIdempotencyState()),
                    "DEAD".equals(wire.getDltStatus()),
                    wire.getDltCategory()));
        }
        return mapped;
    }

    private static EventPeerSignal.IdempotencyState parseIdempotencyState(@Nullable String wireState) {
        if ("COMPLETED".equals(wireState)) {
            return EventPeerSignal.IdempotencyState.COMPLETED;
        }
        if ("IN_PROGRESS".equals(wireState)) {
            return EventPeerSignal.IdempotencyState.IN_PROGRESS;
        }
        return EventPeerSignal.IdempotencyState.ABSENT;
    }

    private static Result<ReconReemitOutcome> mapToReemitOutcome(ReemitOutboxResponse response) {
        String status = response.getStatus() == null ? "UNKNOWN" : response.getStatus();
        return Result.success(new ReconReemitOutcome(response.getReemittedCount(), status));
    }

    private <R extends FcbBaseResponse, T> Result<T> sendAndMap(
            FcbBaseRequest request, Class<R> responseType, Function<R, Result<T>> responseMapper) {

        Result<FcbBaseResponse> result = kafkaClient.sendAndReceive(request, properties.getReconStateTimeout());
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
