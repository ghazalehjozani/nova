package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.NotificationError;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedMessage;
import ir.dotin.platform.pangaea.messaging.requestreply.api.Reply;
import ir.dotin.platform.pangaea.messaging.requestreply.api.ReplyTransportException;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestReplyClient;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestReplyTimeoutException;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestSpec;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config.ArtemisFcbProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.UnReserveCollateralRequest;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class ArtemisFcbRequestReplyClientTest {

    @Mock
    private RequestReplyClient client;

    @Mock
    private RequestSpec spec;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final ArtemisFcbProperties properties = new ArtemisFcbProperties();

    private ArtemisFcbRequestReplyClient sut;

    @BeforeEach
    void setUp() {
        sut = new ArtemisFcbRequestReplyClient(client, objectMapper, properties, meterRegistry);
    }

    private void stubExchangeChain() {
        when(client.destination(any())).thenReturn(spec);
        when(spec.header(any(), any())).thenReturn(spec);
        when(spec.timeout(any())).thenReturn(spec);
    }

    @Test
    void successReplyMapsToSuccessResult() {
        stubExchangeChain();
        String json = "{\"operationName\":\"un-reserve-assurance-for-file\",\"success\":true}";
        when(spec.exchange(any())).thenReturn(new Reply(json.getBytes(UTF_8), Map.of()));

        Result<FcbBaseResponse> result = sut.sendAndReceive(request(), Duration.ofSeconds(5));

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void timeoutMapsToReplyTimeoutError() {
        stubExchangeChain();
        when(spec.exchange(any())).thenThrow(new RequestReplyTimeoutException("x"));

        Result<FcbBaseResponse> result = sut.sendAndReceive(request(), Duration.ofSeconds(5));

        assertThat(result.isSuccess()).isFalse();
        assertThat(errorKeys(result)).contains(CoreBankingErrors.FCB_REPLY_TIMEOUT);
    }

    @Test
    void transportFailureMapsToBrokerUnavailableError() {
        stubExchangeChain();
        when(spec.exchange(any())).thenThrow(new ReplyTransportException("broker down"));

        Result<FcbBaseResponse> result = sut.sendAndReceive(request(), Duration.ofSeconds(5));

        assertThat(result.isSuccess()).isFalse();
        assertThat(errorKeys(result)).contains(CoreBankingErrors.FCB_BROKER_UNAVAILABLE);
    }

    @Test
    void requestSerializationFailureMapsToSerializationError() {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsBytes(any())).thenThrow(mock(JacksonException.class));
        ArtemisFcbRequestReplyClient failingSut =
                new ArtemisFcbRequestReplyClient(client, failingMapper, properties, meterRegistry);

        Result<FcbBaseResponse> result =
                assertThatNoUncheckedEscape(() -> failingSut.sendAndReceive(request(), Duration.ofSeconds(5)));

        assertThat(result.isSuccess()).isFalse();
        assertThat(errorKeys(result)).contains(CoreBankingErrors.FCB_SERIALIZATION_ERROR);
    }

    @Test
    void closeDelegatesToUnderlyingClient() throws Exception {
        RequestReplyClient closeableClient =
                mock(RequestReplyClient.class, withSettings().extraInterfaces(AutoCloseable.class));
        ArtemisFcbRequestReplyClient closeableSut =
                new ArtemisFcbRequestReplyClient(closeableClient, objectMapper, properties, meterRegistry);

        closeableSut.close();

        verify((AutoCloseable) closeableClient).close();
    }

    private static Result<FcbBaseResponse> assertThatNoUncheckedEscape(
            java.util.function.Supplier<Result<FcbBaseResponse>> call) {
        java.util.concurrent.atomic.AtomicReference<Result<FcbBaseResponse>> holder =
                new java.util.concurrent.atomic.AtomicReference<>();
        assertThatCode(() -> holder.set(call.get())).doesNotThrowAnyException();
        return holder.get();
    }

    private static java.util.Set<LocalizedMessage<?>> errorKeys(Result<?> result) {
        return result.err().orElseThrow().notification().errors().stream()
                .map(NotificationError::messageKey)
                .collect(java.util.stream.Collectors.toSet());
    }

    private FcbBaseRequest request() {
        return UnReserveCollateralRequest.builder()
                .assuranceSerial("S1")
                .fileNumber("F1")
                .transactionId("T1")
                .rollBackId("R1")
                .build();
    }
}
