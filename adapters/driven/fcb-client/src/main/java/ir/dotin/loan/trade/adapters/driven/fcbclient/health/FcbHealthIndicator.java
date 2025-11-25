package ir.dotin.loan.trade.adapters.driven.fcbclient.health;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import jakarta.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.adapters.driven.fcbclient.config.FcbConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcbHealthIndicator implements HealthIndicator {

    private final FcbConfiguration fcbConfiguration;
    private final Environment environment;
    private static final int TIMEOUT_MS = 3000;

    private boolean isDevProfile;
    private SSLContext trustAllSslContext;

    @PostConstruct
    public void init() {
        this.isDevProfile = Arrays.asList(environment.getActiveProfiles()).contains("dev");
        if (this.isDevProfile) {
            log.warn("FCB Health Check: SSL Validation DISABLED (Dev Profile Active)");
            initializeSslContext();
        }
    }

    @Override
    public Health health() {
        if (!fcbConfiguration.health().enabled()) {
            return Health.up().withDetail("status", "Disabled by config").build();
        }

        String targetUrl = fcbConfiguration.integration().baseUrl();
        return checkReachability(targetUrl);
    }

    private Health checkReachability(String address) {
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(address).toURL();
            connection = (HttpURLConnection) url.openConnection();

            if (isDevProfile && connection instanceof HttpsURLConnection httpsConnection) {
                configurePermissiveSsl(httpsConnection);
            }

            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            int responseCode = connection.getResponseCode();

            return Health.up()
                    .withDetail("url", address)
                    .withDetail("statusCode", responseCode)
                    .withDetail("reachable", true)
                    .build();

        } catch (IOException e) {
            log.error("FCB Reachability check failed for {}: {}", address, e.getMessage());
            return Health.down()
                    .withDetail("error", e.toString())
                    .withDetail("url", address)
                    .withDetail("reachable", false)
                    .build();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void configurePermissiveSsl(HttpsURLConnection connection) {
        if (trustAllSslContext != null) {
            connection.setSSLSocketFactory(trustAllSslContext.getSocketFactory());
            connection.setHostnameVerifier((hostname, session) -> true);
        }
    }

    private void initializeSslContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAllCerts, new SecureRandom());
            this.trustAllSslContext = ctx;
        } catch (Exception e) {
            log.error("Failed to initialize permissive SSL context", e);
        }
    }
}
