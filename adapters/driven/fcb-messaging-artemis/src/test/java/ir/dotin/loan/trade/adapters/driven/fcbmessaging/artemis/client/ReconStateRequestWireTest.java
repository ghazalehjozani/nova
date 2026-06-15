package ir.dotin.loan.trade.adapters.driven.fcbmessaging.artemis.client;

import java.util.List;

import org.junit.jupiter.api.Test;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReconStateRequest;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** Confirms the Jackson-3 wire serializer actually emits forwardEventUids (LN-59513 live-bug probe). */
class ReconStateRequestWireTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void emitsForwardEventUidsUpcastLikeProduction() {
        FcbBaseRequest req = ReconStateRequest.builder()
                .facilityId("FAC-1")
                .forwardEventUids(List.of("u1", "u2"))
                .build();

        String json = mapper.writeValueAsString(req);

        assertThat(json)
                .as("wire json: %s", json)
                .contains("forwardEventUids")
                .contains("u1")
                .contains("u2");
    }
}
