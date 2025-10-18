package ir.dotin.loan.trade.adapters.driving.rest.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ir.dotin.platform.adapter.security.oauth2.core.client.TpsOAuth2ClientService;
import ir.dotin.platform.adapter.security.oauth2.core.client.TpsOAuth2ClientService.TokenResponse;
import ir.dotin.platform.adapter.security.oauth2.core.config.PlatformSecurityProperties;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("dev")
@Hidden
@RestController
@RequestMapping("/api/dev/auth")
@RequiredArgsConstructor
public class DevTokenController {

    private final TpsOAuth2ClientService oauth2ClientService;
    private final PlatformSecurityProperties securityProperties;

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> generateToken(
            @RequestParam String username, @RequestParam String password) {
        var devConfig = securityProperties.devAuth();
        if (devConfig == null || !devConfig.enabled()) {
            return ResponseEntity.badRequest().build();
        }
        log.info("Generating dev token for user={} with branch_code={}", username, devConfig.branchCode());
        Map<String, Object> customClaims = Map.of("branch_code", devConfig.branchCode());
        TokenResponse token = oauth2ClientService.getPasswordToken(username, password, customClaims);
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", token.accessToken());
        response.put("token_type", token.tokenType());
        response.put("expires_in", token.expiresIn());
        if (token.scope() != null) response.put("scope", token.scope());
        if (token.refreshToken() != null) response.put("refresh_token", token.refreshToken());
        return ResponseEntity.ok(response);
    }
}
