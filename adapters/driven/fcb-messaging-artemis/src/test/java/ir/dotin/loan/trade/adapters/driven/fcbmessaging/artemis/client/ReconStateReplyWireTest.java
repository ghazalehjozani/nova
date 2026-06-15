package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconPeerSignal;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconStateResponse;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confirms nova deserializes peerSignals/dltPresentForFacility from the FCB recon-state reply (LN-59513 live-bug
 * probe).
 */
class ReconStateReplyWireTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void deserializesPeerSignalsAndDltFlag() {
        String json = "{\"operationName\":\"nova-loanfile-recon-state\",\"success\":true,\"statusCode\":200,"
                + "\"exists\":false,\"reachable\":true,\"dltPresentForFacility\":true,"
                + "\"peerSignals\":[{\"eventUid\":\"u1\",\"idempotencyState\":\"COMPLETED\",\"dltStatus\":\"NONE\",\"dltCategory\":null},"
                + "{\"eventUid\":\"u2\",\"idempotencyState\":\"ABSENT\",\"dltStatus\":\"DEAD\",\"dltCategory\":\"PERMANENT_HTTP\"}]}";

        FcbBaseResponse resp = mapper.readValue(json, FcbBaseResponse.class);

        assertThat(resp).as("reply parsed type").isInstanceOf(ReconStateResponse.class);
        ReconStateResponse r = (ReconStateResponse) resp;
        assertThat(r.isDltPresentForFacility()).as("dltPresentForFacility").isTrue();

        List<ReconPeerSignal> signals = Objects.requireNonNull(r.getPeerSignals(), "peerSignals was null");
        assertThat(signals).hasSize(2);
        assertThat(signals.get(1).getDltStatus()).isEqualTo("DEAD");
        assertThat(signals.get(1).getDltCategory()).isEqualTo("PERMANENT_HTTP");
        assertThat(signals.get(0).getIdempotencyState()).isEqualTo("COMPLETED");
    }
}
