package ir.dotin.loan.trade.config.fcb;

import java.security.PublicKey;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.envelope.impl.jws.config.EnvelopeProperties;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.Jwk;
import ir.dotin.platform.pangaea.envelope.impl.jws.jwks.RedisTrustedKeyStore;
import ir.dotin.platform.pangaea.messaging.requestreply.api.Reply;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestReplyClient;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestReplyTimeoutException;
import ir.dotin.platform.pangaea.messaging.requestreply.api.RequestSpec;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.config.ArtemisFcbProperties;

import tools.jackson.databind.ObjectMapper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtemisJwksFetcherTest {

    private static final String KID = "kid-1";

    @Mock
    private RequestReplyClient client;

    @Mock
    private RequestSpec spec;

    @Mock
    private RedisTrustedKeyStore trustedKeyStore;

    @Mock
    private PublicKey publicKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArtemisFcbProperties properties = new ArtemisFcbProperties();
    private final EnvelopeProperties.Kafka jwksConfig = new EnvelopeProperties.Kafka();

    private ArtemisJwksFetcher sut;

    @BeforeEach
    void setUp() {
        jwksConfig.setOperationType("jwks-fetch");
        properties.setReplyTimeout(Duration.ofSeconds(5));
        sut = new ArtemisJwksFetcher(client, properties, objectMapper, jwksConfig, trustedKeyStore, null);
    }

    @Test
    void validReplyStoresKeyAndReturnsTrue() {
        when(client.destination(any())).thenReturn(spec);
        when(spec.header(any(), any())).thenReturn(spec);
        when(spec.timeout(any())).thenReturn(spec);
        String json = "{\"requestId\":\"r1\",\"keys\":[{\"kty\":\"RSA\",\"kid\":\"" + KID + "\"}]}";
        when(spec.exchange(any())).thenReturn(new Reply(json.getBytes(UTF_8), Map.of()));
        when(trustedKeyStore.get(KID)).thenReturn(Optional.empty()).thenReturn(Optional.of(publicKey));

        boolean result = sut.fetch(KID);

        assertThat(result).isTrue();
        ArgumentCaptor<Jwk> captor = ArgumentCaptor.forClass(Jwk.class);
        verify(trustedKeyStore).putJwk(captor.capture());
        assertThat(captor.getValue().kid()).isEqualTo(KID);
    }

    @Test
    void timeoutReturnsFalseAndStoresNothing() {
        when(client.destination(any())).thenReturn(spec);
        when(spec.header(any(), any())).thenReturn(spec);
        when(spec.timeout(any())).thenReturn(spec);
        when(spec.exchange(any())).thenThrow(new RequestReplyTimeoutException("timed out"));
        when(trustedKeyStore.get(KID)).thenReturn(Optional.empty());

        boolean result = sut.fetch(KID);

        assertThat(result).isFalse();
        verify(trustedKeyStore, never()).putJwk(any());
    }
}
