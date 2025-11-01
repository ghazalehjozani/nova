package ir.dotin.loan.trade.adapters.driven.fcbclient.health;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbclient.config.FcbConfiguration;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.service.FcbService;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FcbHealthIndicator implements HealthIndicator {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;
    private final FcbConfiguration fcbConfiguration;
    private final MeterRegistry meterRegistry;

    // Metrics
    private final Counter healthCheckSuccessCounter;
    private final Counter healthCheckFailureCounter;
    private final Timer healthCheckTimer;

    // State tracking
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicReference<HealthCheckResult> lastSuccessfulCheck = new AtomicReference<>();
    private volatile Instant lastCheckTime;

    public FcbHealthIndicator(
            FcbService fcbService,
            FcbBaseRequestBuilder requestBuilder,
            FcbConfiguration fcbConfiguration,
            MeterRegistry meterRegistry) {

        this.fcbService = fcbService;
        this.requestBuilder = requestBuilder;
        this.fcbConfiguration = fcbConfiguration;
        this.meterRegistry = meterRegistry;

        // Initialize metrics
        this.healthCheckSuccessCounter = Counter.builder("fcb.health.check.success")
                .description("Number of successful FCB health checks")
                .register(meterRegistry);

        this.healthCheckFailureCounter = Counter.builder("fcb.health.check.failure")
                .description("Number of failed FCB health checks")
                .register(meterRegistry);

        this.healthCheckTimer = Timer.builder("fcb.health.check.duration")
                .description("Duration of FCB health check execution")
                .register(meterRegistry);
    }

    @Override
    public Health health() {
        if (!fcbConfiguration.health().enabled()) {
            return Health.up()
                    .withDetail("status", "disabled")
                    .withDetail("message", "FCB health check is disabled")
                    .build();
        }

        try {
            // Check if we can use cached result
            if (isCacheValid()) {
                log.debug("Using cached FCB health check result");
                return buildHealthFromCache();
            }

            // Perform actual health check with timing
            HealthCheckResult result = healthCheckTimer.record(() -> performHealthCheck());

            // Update state based on result
            updateState(result);

            return buildHealth(result, false);

        } catch (Exception e) {
            log.error("Unexpected error during FCB health check", e);
            consecutiveFailures.incrementAndGet();
            healthCheckFailureCounter.increment();

            return Health.down()
                    .withDetail("error", "Health check failed")
                    .withDetail("message", e.getMessage())
                    .withDetail("timestamp", Instant.now().toString())
                    .withDetail("consecutiveFailures", consecutiveFailures.get())
                    .build();
        }
    }

    private HealthCheckResult performHealthCheck() {
        Instant startTime = Instant.now();
        HealthCheckResult result = new HealthCheckResult();
        result.setCheckTime(startTime);
        result.setFcbBaseUrl(fcbConfiguration.integration().baseUrl());
        result.setFcbServicePath(fcbConfiguration.integration().servicePath());

        try {
            log.debug(
                    "Performing FCB health check - attempting connection to {}",
                    fcbConfiguration.integration().getFullServiceUrl());

            // Execute lightweight test usecase with timeout
            Result<FcbBaseResponse> testResult = executeTestUsecaseWithTimeout();

            Duration responseTime = Duration.between(startTime, Instant.now());
            result.setResponseTimeMs(responseTime.toMillis());

            if (!testResult.isFailure() && testResult.value() != null) {
                FcbBaseResponse response = testResult.value();
                result.setHealthy(response.isSuccess());
                result.setMessage(response.isSuccess() ? "FCB connection healthy" : response.getErrorDescription());
                result.setTransactionCode(response.getTransactionCode());
                result.setRsCode(response.getRsCode());
                result.setResponseDateTime(response.getResponseDateTime());

                if (response.isSuccess()) {
                    log.info("FCB health check passed - Response time: {}ms", result.getResponseTimeMs());
                } else {
                    log.warn(
                            "FCB health check failed - rsCode: {}, transactionCode: {}, message: {}",
                            response.getRsCode(),
                            response.getTransactionCode(),
                            result.getMessage());
                }
            } else {
                result.setHealthy(false);
                String errorMessage = testResult.notification() != null
                        ? testResult.notification().getErrorMessages().toString()
                        : "Unknown error";
                result.setMessage("FCB service returned failure: " + errorMessage);
                result.setErrorDetails(errorMessage);
                log.error("FCB health check failed: {}", errorMessage);
            }

        } catch (java.util.concurrent.TimeoutException e) {
            log.error(
                    "FCB health check timed out after {} seconds",
                    fcbConfiguration.health().timeoutSeconds());
            result.setHealthy(false);
            result.setMessage(
                    "Health check timed out after " + fcbConfiguration.health().timeoutSeconds() + " seconds");
            result.setError("TimeoutException");
            result.setErrorDetails(e.getMessage());
        } catch (Exception e) {
            log.error("FCB health check failed with unexpected exception", e);
            result.setHealthy(false);
            result.setMessage("Connection test failed: " + e.getMessage());
            result.setError(e.getClass().getSimpleName());
            result.setErrorDetails(e.toString());
        }

        return result;
    }

    private Result<FcbBaseResponse> executeTestUsecaseWithTimeout() throws java.util.concurrent.TimeoutException {

        try {
            // Create a minimal test request
            Usecases usecases = requestBuilder.buildUseCase(
                    fcbConfiguration.health().testUsecase(), java.util.Collections.emptyList());

            FcbRequest request = FcbRequest.builder().usecase(usecases).build();

            // Execute with timeout using CompletableFuture
            java.util.concurrent.CompletableFuture<Result<FcbBaseResponse>> future =
                    java.util.concurrent.CompletableFuture.supplyAsync(
                            () -> fcbService.executeUsecase(request, FcbBaseResponse.class));

            return future.get(fcbConfiguration.health().timeoutSeconds(), TimeUnit.SECONDS);

        } catch (java.util.concurrent.TimeoutException e) {
            log.error(
                    "FCB test usecase timed out after {} seconds",
                    fcbConfiguration.health().timeoutSeconds());
            throw e;
        } catch (Exception e) {
            log.error("Test usecase execution failed", e);
            return Result.failure(ir.dotin.platform.commons.core.Notification.ofError(
                    ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes
                            .FCB_UNKNOWN_ERROR,
                    e.getMessage()));
        }
    }

    private void updateState(HealthCheckResult result) {
        lastCheckTime = Instant.now();

        if (result.isHealthy()) {
            consecutiveFailures.set(0);
            lastSuccessfulCheck.set(result);
            healthCheckSuccessCounter.increment();
        } else {
            consecutiveFailures.incrementAndGet();
            healthCheckFailureCounter.increment();
        }

        // Update gauge metrics
        meterRegistry.gauge("fcb.health.consecutive.failures", consecutiveFailures.get());
        meterRegistry.gauge(
                "fcb.health.response.time", result.getResponseTimeMs() != null ? result.getResponseTimeMs() : 0);
    }

    private boolean isCacheValid() {
        if (lastCheckTime == null) {
            return false;
        }

        Duration cacheDuration = Duration.ofSeconds(fcbConfiguration.health().cacheDurationSeconds());
        Duration timeSinceLastCheck = Duration.between(lastCheckTime, Instant.now());

        return timeSinceLastCheck.compareTo(cacheDuration) < 0;
    }

    private Health buildHealthFromCache() {
        HealthCheckResult cachedResult = lastSuccessfulCheck.get();
        if (cachedResult != null) {
            return buildHealth(cachedResult, true);
        }

        // No cached result available, perform actual check
        return health();
    }

    private Health buildHealth(HealthCheckResult result, boolean fromCache) {
        // Determine overall health status based on consecutive failures
        boolean isDown = !result.isHealthy()
                || consecutiveFailures.get() >= fcbConfiguration.health().failureThreshold();

        Health.Builder builder = isDown ? Health.down() : Health.up();

        Map<String, Object> details = new HashMap<>();

        // Basic information
        details.put("service", "FCB Core Banking System");
        details.put("status", isDown ? "DOWN" : "UP");
        details.put("message", result.getMessage());
        details.put("checkTime", result.getCheckTime().toString());
        details.put("cached", fromCache);

        // Connection details
        Map<String, Object> connectionInfo = new HashMap<>();
        connectionInfo.put("baseUrl", result.getFcbBaseUrl());
        connectionInfo.put("servicePath", result.getFcbServicePath());
        connectionInfo.put(
                "fullUrl",
                result.getFcbBaseUrl() + "/" + fcbConfiguration.integration().appName() + "/"
                        + result.getFcbServicePath());
        details.put("connection", connectionInfo);

        // Performance metrics
        if (result.getResponseTimeMs() != null) {
            details.put("responseTimeMs", result.getResponseTimeMs());
            details.put(
                    "performanceStatus",
                    result.getResponseTimeMs() < 1000
                            ? "EXCELLENT"
                            : result.getResponseTimeMs() < 3000
                                    ? "GOOD"
                                    : result.getResponseTimeMs() < 5000 ? "ACCEPTABLE" : "SLOW");
        }

        // Failure tracking
        Map<String, Object> failureInfo = new HashMap<>();
        failureInfo.put("consecutiveFailures", consecutiveFailures.get());
        failureInfo.put("failureThreshold", fcbConfiguration.health().failureThreshold());
        failureInfo.put(
                "thresholdReached",
                consecutiveFailures.get() >= fcbConfiguration.health().failureThreshold());
        details.put("failures", failureInfo);

        // FCB response details (only if showDetails is enabled)
        if (fcbConfiguration.health().showDetails()) {
            if (result.getTransactionCode() != null) {
                details.put("transactionCode", result.getTransactionCode());
            }

            if (result.getRsCode() != null) {
                details.put("rsCode", result.getRsCode());
            }

            if (result.getResponseDateTime() != null) {
                details.put("fcbResponseTime", result.getResponseDateTime());
            }

            if (result.getError() != null) {
                Map<String, Object> errorInfo = new HashMap<>();
                errorInfo.put("type", result.getError());
                errorInfo.put("details", result.getErrorDetails());
                details.put("error", errorInfo);
            }
        }

        // Last successful check information
        HealthCheckResult lastSuccess = lastSuccessfulCheck.get();
        if (lastSuccess != null) {
            Map<String, Object> lastSuccessInfo = new HashMap<>();
            lastSuccessInfo.put("time", lastSuccess.getCheckTime().toString());
            lastSuccessInfo.put("responseTimeMs", lastSuccess.getResponseTimeMs());
            details.put("lastSuccessfulCheck", lastSuccessInfo);
        }

        details.forEach(builder::withDetail);

        return builder.build();
    }

    @Data
    private static class HealthCheckResult {
        private boolean healthy;
        private String message;
        private Instant checkTime;
        private Long responseTimeMs;
        private String transactionCode;
        private String rsCode;
        private String responseDateTime;
        private String error;
        private String errorDetails;
        private String fcbBaseUrl;
        private String fcbServicePath;
    }
}
