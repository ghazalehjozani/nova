package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request;

import java.util.List;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtraInfoVO {

    @JsonProperty("type")
    private String type;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token")
    private String token;

    @JsonProperty("userMetaData")
    private List<Map<String, Object>> userMetaData;

    @JsonProperty("systemMetaData")
    private Map<String, Object> systemMetaData;

    /** Convert this object to JSON string for FCB request */
    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("Error converting ExtraInfoVO to JSON", e);
            return "{}";
        }
    }

    /** Parse JSON string to ExtraInfoVO object */
    public static ExtraInfoVO fromJsonString(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, ExtraInfoVO.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing ExtraInfoVO from JSON: {}", json, e);
            return null;
        }
    }
}
