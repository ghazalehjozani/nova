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
import tools.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        when(client.destination(any())).thenReturn(spec);
        when(spec.header(any(), any())).thenReturn(spec);
        when(spec.timeout(any())).thenReturn(spec);
    }

    @Test
    void successReplyMapsToSuccessResult() {
        String json = "{\"operationName\":\"un-reserve-assurance-for-file\",\"success\":true}";
        when(spec.exchange(any())).thenReturn(new Reply(json.getBytes(UTF_8), Map.of()));

        Result<FcbBaseResponse> result = sut.sendAndReceive(request(), Duration.ofSeconds(5));

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void timeoutMapsToReplyTimeoutError() {
        when(spec.exchange(any())).thenThrow(new RequestReplyTimeoutException("x"));

        Result<FcbBaseResponse> result = sut.sendAndReceive(request(), Duration.ofSeconds(5));

        assertThat(result.isSuccess()).isFalse();
        assertThat(errorKeys(result)).contains(CoreBankingErrors.FCB_REPLY_TIMEOUT);
    }

    @Test
    void transportFailureMapsToBrokerUnavailableError() {
        when(spec.exchange(any())).thenThrow(new ReplyTransportException("broker down"));

        Result<FcbBaseResponse> result = sut.sendAndReceive(request(), Duration.ofSeconds(5));

        assertThat(result.isSuccess()).isFalse();
        assertThat(errorKeys(result)).contains(CoreBankingErrors.FCB_BROKER_UNAVAILABLE);
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
