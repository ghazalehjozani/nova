package ir.dotin.loan.trade.e2e;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import ir.dotin.loan.trade.e2e.orchestrator.MockPortConfigurator;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator;

// The E2E harness had rotted on several independent layers while it sat @Disabled since 980c500c.
// Repaired here, and all of it is now exercised: no-SSO auth context, Consul turned off in the bootstrap
// context, the socat ambassador dropped (it cannot coexist with `network_mode: host`), the redis-cluster
// bootstrap sidecar's shell script (Compose was word-splitting it), a static datasource/Kafka config, the
// Consul-supplied cache-version and FCB-topic keys mirrored into application-e2e.yml, Consul-backed reply
// partition leasing disabled, one FCB port mock covering all seven interfaces FcbValidationAdapter
// implements, JwtDecoder/Jwt-converter/ServiceTokenProvider stubs for the disabled security starter, and a
// JaCoCo exclusion for jsqlparser. The application context now starts end to end.

@AutoConfigureTestRestTemplate
public abstract class AbstractRestE2E extends AbstractE2E {

    // Request bodies are NOT serialized with the autowired application mapper. That one carries pangaea's
    // WireSerializationModule, which renders every LocalizedEnum as a {code,label} object — correct for a
    // response, wrong for a request. PartyRequestDto is polymorphic on `role` as an EXISTING_PROPERTY type
    // id, so the object form makes the server answer 400 LOAN-201 ("missing type id property 'role'"). Real
    // clients send the bare enum name (see nova-testkit's Bruno payloads); this mapper does the same. Money
    // goes out as a JSON number, which the wire MoneyStringDeserializer accepts alongside strings.
    private static final ObjectMapper REQUEST_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected MockPortConfigurator mockPortConfigurator;

    @Autowired
    protected PrerequisiteOrchestrator prerequisiteOrchestrator;

    @BeforeEach
    void setupMocksForRest() {
        mockPortConfigurator.configureAllDefaults();
    }

    protected String apiUrl(String path) {
        return "http://localhost:" + port + "/v1" + path;
    }

    protected HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        // Required by DispatchContextFilter; without it every command answers 400 LOAN-203.
        headers.set("X-Correlation-ID", UUID.randomUUID().toString());
        headers.set("X-Request-DateTime", java.time.Instant.now().toString());
        headers.set("Accept-Language", "fa");
        if (authToken != null && !authToken.isBlank()) {
            headers.set("Authorization", authToken);
        }
        return headers;
    }

    protected HttpHeaders defaultHeadersWithIdempotencyKey(String idempotencyKey) {
        HttpHeaders headers = defaultHeaders();
        headers.set("Idempotency-Key", idempotencyKey);
        return headers;
    }

    protected ResponseEntity<String> postJson(String path, Object body) {
        HttpHeaders headers = defaultHeaders();
        String json;
        try {
            json = REQUEST_MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return restTemplate.postForEntity(apiUrl(path), entity, String.class);
    }

    protected ResponseEntity<String> postJsonWithHeaders(String path, Object body, HttpHeaders headers) {
        String json;
        try {
            json = REQUEST_MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return restTemplate.postForEntity(apiUrl(path), entity, String.class);
    }

    protected ResponseEntity<String> getJson(String path) {
        HttpHeaders headers = defaultHeaders();
        // Idempotency-Key and X-Correlation-ID are command-only (HeaderPolicy); queries neither send nor echo them.
        headers.remove("Idempotency-Key");
        headers.remove("X-Correlation-ID");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(apiUrl(path), HttpMethod.GET, entity, String.class);
    }
}
