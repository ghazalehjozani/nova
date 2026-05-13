package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = FcbConfiguration.BASE)
public class FcbConfiguration {

    public static final String BASE = "fcb";

    @NotNull
    private Integration integration;

    @NotNull
    private Health health;

    @Data
    public static class Integration {
        @NotBlank
        private String baseUrl;

        @NotBlank
        private String appName;

        @NotBlank
        private String servicePath;

        @NotNull
        private Integer connectionTimeout;

        @NotNull
        private Integer readTimeout;

        @NotNull
        private Credentials credentials;

        private String encoding = "UTF-8";

        private boolean showExceptions = true;

        private boolean sameSession = false;

        public String getFullServiceUrl() {
            return String.format("%s/%s/%s", baseUrl, appName, servicePath);
        }
    }

    @Data
    public static class Health {
        private boolean enabled = true;

        @NotBlank
        private String testUsecase = "system-health-check";

        private int timeoutSeconds = 10;

        private int cacheDurationSeconds = 30;

        private int failureThreshold = 3;

        private boolean showDetails = true;

        private boolean metricsEnabled = true;
    }

    @Data
    public static class Credentials {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }
}
