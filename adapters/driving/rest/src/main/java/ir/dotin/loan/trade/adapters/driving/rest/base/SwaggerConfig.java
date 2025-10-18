package ir.dotin.loan.trade.adapters.driving.rest.base;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    @Profile("dev")
    public OpenAPI devOpenAPI() {
        return baseOpenAPI()
                .components(createDevComponents())
                .addSecurityItem(new SecurityRequirement().addList("dev-auto-auth"));
    }

    @Bean
    @Profile("!dev")
    public OpenAPI prodOpenAPI() {
        return baseOpenAPI()
                .components(createProdComponents())
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    private OpenAPI baseOpenAPI() {
        return new OpenAPI().info(apiInfo()).servers(List.of(createServer()));
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

    private Components createDevComponents() {
        return new Components()
                .addSecuritySchemes("dev-auto-auth", createDevAutoAuthScheme())
                .addSecuritySchemes("bearer-jwt", createBearerScheme());
    }

    private Components createProdComponents() {
        return new Components().addSecuritySchemes("bearer-jwt", createBearerScheme());
    }

    private SecurityScheme createBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT Bearer Token from TPS SSO");
    }

    private SecurityScheme createDevAutoAuthScheme() {
        String tokenUrl = contextPath.isEmpty() ? "/api/dev/auth/token" : contextPath + "/api/dev/auth/token";

        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .password(new OAuthFlow().tokenUrl(tokenUrl).scopes(new Scopes())))
                .description("client_id and client_secret is optional");
    }
}
