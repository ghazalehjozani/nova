package ir.dotin.loan.trade.e2e;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

// TODO(CBS-274722:LN-58922:LN-59021): still disabled, but two of the three original blockers are gone.
// FIXED here: (1) the auth TODO — the e2e profile already runs with pangaea.security.enabled=false, and
// E2ETestConfiguration now supplies a fixed AuthenticationContextHolder, so no SSO token is needed;
// (2) Consul — application-e2e.yml disables it and AbstractE2E turns the bootstrap context off, so the
// ${CONSUL_PORT} placeholder in the main bootstrap.yml is never bound.
// REMAINING: the context fails with BeanDefinitionOverrideException on 'jpaAuditingHandler' — the
// persistence starter's @EnableJpaAuditing is registered twice. Disabling the bootstrap context does NOT
// fix it, so it is not a bootstrap/main double-registration. Production boots fine, so this is specific to
// the @SpringBootTest(classes = {NovaApplication, E2ETestConfiguration}) context assembly. Diagnosing it is
// its own piece of work and belongs to the ticket that disabled this harness, not to
// CBS-282143:LN-59253:LN-59261:LN-59661.
@Disabled
@AutoConfigureTestRestTemplate
public abstract class AbstractRestE2E extends AbstractE2E {

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
            json = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return restTemplate.postForEntity(apiUrl(path), entity, String.class);
    }

    protected ResponseEntity<String> postJsonWithHeaders(String path, Object body, HttpHeaders headers) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return restTemplate.postForEntity(apiUrl(path), entity, String.class);
    }

    protected ResponseEntity<String> getJson(String path) {
        HttpHeaders headers = defaultHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(apiUrl(path), HttpMethod.GET, entity, String.class);
    }
}
