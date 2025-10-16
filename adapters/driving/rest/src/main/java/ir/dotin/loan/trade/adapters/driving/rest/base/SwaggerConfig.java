package ir.dotin.loan.trade.adapters.driving.rest.base;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.adapter.security.oauth2.core.config.PlatformSecurityProperties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final PlatformSecurityProperties securityProperties;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(createServer()))
                .components(createComponents())
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    private Info apiInfo() {
        return new Info()
                .title(applicationName)
                .version(applicationVersion)
                .description("Trade Loan API")
                .contact(new Contact().name("Loan Team"));
    }

    private Server createServer() {
        return new Server().url(contextPath.isEmpty() ? "/" : contextPath).description("Current Environment");
    }

    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("bearer-jwt", createBearerScheme())
                .addSecuritySchemes("oauth2-client-credentials", createClientCredentialsScheme());
    }

    private SecurityScheme createBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer Token from SSO (Client Credentials flow)");
    }

    private SecurityScheme createClientCredentialsScheme() {
        var tokenUri = securityProperties.oauth2Client().tokenUri();
        var scopes = securityProperties.oauth2Client().scopes();

        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("OAuth2 Client Credentials")
                .flows(new OAuthFlows()
                        .clientCredentials(new OAuthFlow().tokenUrl(tokenUri).scopes(createScopes(scopes))));
    }

    private Scopes createScopes(List<String> configuredScopes) {
        Scopes scopes = new Scopes();
        configuredScopes.forEach(scope -> scopes.addString(scope, getScopeDescription(scope)));
        return scopes;
    }

    private String getScopeDescription(String scope) {
        return switch (scope) {
            case "core" -> "Core banking operations";
            case "client_credentials" -> "Client credentials flow";
            case "upgrade_token" -> "Token privilege escalation";
            default -> scope + " operations";
        };
    }
}
