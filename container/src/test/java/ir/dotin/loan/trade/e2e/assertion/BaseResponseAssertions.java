package ir.dotin.loan.trade.e2e.assertion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public final class BaseResponseAssertions {

    private BaseResponseAssertions() {}

    public static void assertSuccess(ResponseEntity<String> response, ObjectMapper om) {
        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("Expected 2xx status but got %s: %s", response.getStatusCode(), response.getBody())
                .isTrue();
        JsonNode body = parseBody(response, om);
        assertThat(body.has("data"))
                .as("Response should have 'data' field: %s", response.getBody())
                .isTrue();
    }

    public static void assertSuccessWithStatus(ResponseEntity<String> response, int expectedStatus, ObjectMapper om) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(expectedStatus));
        JsonNode body = parseBody(response, om);
        assertThat(body.has("data"))
                .as("Response should have 'data' field: %s", response.getBody())
                .isTrue();
    }

    public static void assertError(ResponseEntity<String> response, int httpStatus) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(httpStatus));
    }

    public static void assertErrorWithPrefix(
            ResponseEntity<String> response, int httpStatus, String errorCodePrefix, ObjectMapper om) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(httpStatus));
        JsonNode body = parseBody(response, om);
        if (body.has("errors")
                && body.get("errors").isArray()
                && !body.get("errors").isEmpty()) {
            String code = body.get("errors").get(0).path("code").asText("");
            assertThat(code).startsWith(errorCodePrefix);
        }
    }

    public static JsonNode extractData(ResponseEntity<String> response, ObjectMapper om) {
        JsonNode body = parseBody(response, om);
        assertThat(body.has("data"))
                .as("Response should have 'data' field: %s", response.getBody())
                .isTrue();
        return body.get("data");
    }

    public static JsonNode parseBody(ResponseEntity<String> response, ObjectMapper om) {
        assertThat(response.getBody()).as("Response body should not be null").isNotNull();
        try {
            return om.readTree(response.getBody());
        } catch (Exception e) {
            throw new AssertionError("Failed to parse response body as JSON: " + response.getBody(), e);
        }
    }
}
