package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtraInfoVO {

    private String type;

    private String scope;

    private String token;

    private List<Map<String, Object>> userMetaData;

    private Map<String, Object> systemMetaData;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String toJsonString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ExtraInfoVO to JSON", e);
            throw new RuntimeException("Failed to serialize extra info", e);
        }
    }

    public static ExtraInfoVO fromJsonString(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, ExtraInfoVO.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize ExtraInfoVO from JSON: {}", json, e);
            throw new RuntimeException("Failed to deserialize extra info", e);
        }
    }
}
