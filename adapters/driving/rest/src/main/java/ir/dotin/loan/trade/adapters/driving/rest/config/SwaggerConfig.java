package ir.dotin.loan.trade.adapters.driving.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ir.dotin.platform.adapter.rest.swagger.BaseSwaggerConfig;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig extends BaseSwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Bean
    public OpenAPI customOpenAPI() {
        return createOpenAPI().info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title(applicationName)
                .version(applicationVersion)
                .description("Trade Loan API")
                .contact(new Contact().name("Loan Team"));
    }

    @Override
    protected String getContextPath() {
        return contextPath;
    }

    @Override
    protected String getTokenUrl() {
        return contextPath.isEmpty() ? "/api/dev/auth/token" : contextPath + "/api/dev/auth/token";
    }

    @Override
    protected boolean isDevProfile() {
        return "dev".equals(activeProfile);
    }
}
