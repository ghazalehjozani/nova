package ir.dotin.loan.trade.adapters.driving.rest.auth;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import ir.dotin.platform.adapter.security.oauth2.core.config.PlatformSecurityProperties;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile({"dev", "stage"})
@Hidden
@RestController
@RequestMapping("/api/dev/auth")
@RequiredArgsConstructor
public class DevAuthCallbackController {

    private final ObjectMapper objectMapper;
    private final PlatformSecurityProperties securityProperties;

    @PostMapping("/callback")
    public ResponseEntity<?> tokenProxy(
            @RequestParam String code,
            @RequestParam(name = "redirect_uri") String redirectUri,
            @RequestParam(name = "client_id") String clientId) {

        var devConfig = securityProperties.devAuth();
        var oauth2Config = securityProperties.oauth2Client();

        log.info("Token exchange - code={} redirect_uri={}", maskCode(code), redirectUri);

        try {
            Map<String, Object> customClaims = Map.of("branch_code", devConfig.branchCode());
            String claimsJson = objectMapper.writeValueAsString(customClaims);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("client_id", clientId);
            body.add("client_secret", oauth2Config.clientSecret());
            body.add("redirect_uri", redirectUri);
            body.add("client_claims", claimsJson);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(oauth2Config.tokenUri(), new HttpEntity<>(body, headers), Map.class);

            log.info("Token exchange successful");
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            log.error("Token exchange failed", e);
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    private String maskCode(String code) {
        if (code == null || code.length() < 8) return "****";
        return code.substring(0, 4) + "****";
    }
}
