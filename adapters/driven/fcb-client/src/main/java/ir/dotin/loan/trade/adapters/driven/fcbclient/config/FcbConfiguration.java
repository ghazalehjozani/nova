package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = FcbConfiguration.BASE)
public record FcbConfiguration(
        @NotNull Integration integration, @NotNull Health health) {

    public static final String BASE = "fcb";

    public record Integration(
            @NotBlank String baseUrl,
            @NotBlank String appName,
            @NotBlank String servicePath,
            @NotNull Integer connectionTimeout,
            @NotNull Integer readTimeout,
            @NotNull Credentials credentials,
            @DefaultValue("UTF-8") String encoding,
            @DefaultValue("true") boolean showExceptions,
            @DefaultValue("false") boolean sameSession) {

        public String getFullServiceUrl() {
            return String.format("%s/%s/%s", baseUrl, appName, servicePath);
        }
    }

    public record Health(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("system-health-check") @NotBlank String testUsecase,
            @DefaultValue("10") int timeoutSeconds,
            @DefaultValue("30") int cacheDurationSeconds,
            @DefaultValue("3") int failureThreshold,
            @DefaultValue("true") boolean showDetails,
            @DefaultValue("true") boolean metricsEnabled) {}

    public record Credentials(
            @NotBlank String username, @NotBlank String password) {}
}
