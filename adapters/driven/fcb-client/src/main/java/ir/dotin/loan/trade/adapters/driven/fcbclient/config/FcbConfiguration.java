package ir.dotin.loan.trade.adapters.driven.fcbclient.config;

import java.util.concurrent.TimeUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "fcb.integration")
public class FcbConfiguration {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String appName;

    @NotBlank
    private String servicePath;

    @NotNull
    private Integer connectionTimeout = (int) TimeUnit.MINUTES.toMillis(5);

    @NotNull
    private Integer readTimeout = (int) TimeUnit.MINUTES.toMillis(6);

    private Credentials credentials;

    private String encoding = "UTF-8";

    private boolean showExceptions = true;

    private boolean sameSession = false;

    @Data
    public static class Credentials {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    public String getFullServiceUrl() {
        return String.format("%s/%s/%s", baseUrl, appName, servicePath);
    }
}
