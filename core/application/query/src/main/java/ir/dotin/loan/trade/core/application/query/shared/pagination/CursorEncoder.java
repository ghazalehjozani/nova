package ir.dotin.loan.trade.core.application.query.shared.pagination;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.core.application.query.shared.exception.InvalidCursorException;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CursorEncoder {
    private final ObjectMapper objectMapper;

    public String encode(CursorPosition position) {
        String json = objectMapper.writeValueAsString(position);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public CursorPosition decode(String cursor) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, CursorPosition.class);
        } catch (Exception e) {
            throw new InvalidCursorException("Invalid cursor format", e);
        }
    }

    public void validate(String cursor) {
        if (cursor == null || cursor.isBlank()) return;
        try {
            decode(cursor);
        } catch (Exception e) {
            throw new InvalidCursorException("Malformed cursor", e);
        }
    }
}
