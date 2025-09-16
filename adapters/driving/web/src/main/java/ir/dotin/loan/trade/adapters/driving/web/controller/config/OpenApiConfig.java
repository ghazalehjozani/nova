package ir.dotin.loan.trade.adapters.driving.web.controller.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Value("${api.version:1.0.0}")
    private String apiVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Trade Loan")
                        .version(apiVersion)
                        .description("Trade Loan API")
                        .contact(new Contact().name("Loan Team").email("m.amirabdollahi@dotin.ir"))
                        .license(new License().name("Proprietary").url("https://dotin.ir/")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://api-dev.dotin.ir").description("Development"),
                        new Server().url("https://api.dotin.ir").description("Production")));
    }
}
