package ir.dotin.loan.trade.adapters.driven.fcbmessaging.client;

import java.time.Duration;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;

/**
 * Transport-neutral request/reply seam to the legacy FCB core.
 *
 * <p>This is the single switch point between corridor transports: the port-implementing adapters
 * ({@code FcbAccountAdapter}, {@code FcbTransactionAdapter}, {@code FcbValidationAdapter},
 * {@code FcbReconStateAdapter}) depend only on this interface and never on a concrete transport client. A Kafka
 * implementation lives in {@code fcb-messaging-kafka} and an ActiveMQ Artemis implementation in
 * {@code fcb-messaging-artemis}; the active transport is selected by a delegating router in the composition root
 * (Consul property {@code nova.fcb.transport-mode}). Neither transport module references the other.
 *
 * <p>The wire payload ({@link FcbKafkaBaseRequest} / {@link FcbKafkaBaseResponse}) is plain polymorphic JSON keyed on
 * {@code operationName}; it is identical on either transport, which is why both reach the same FCB command handler.
 */
public interface FcbRequestReplyClient {

    /**
     * Send a request to FCB and synchronously await its reply.
     *
     * @param request the polymorphic FCB request (its {@code operationName} routes it on the FCB side).
     * @param timeout per-call wall-time budget for the reply.
     * @return {@link Result#success} carrying the typed reply, or {@link Result#failure} with a
     *     {@code CoreBankingErrors} code on transport/timeout/server/business failure.
     */
    Result<FcbKafkaBaseResponse> sendAndReceive(FcbKafkaBaseRequest request, Duration timeout);
}
