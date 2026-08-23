package ir.dotin.loan.trade.e2e;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ir.dotin.platform.pangaea.security.api.AuthenticationContextHolder;
import ir.dotin.platform.pangaea.security.api.OAuth2TokenResponse;
import ir.dotin.platform.pangaea.security.api.ServiceTokenProvider;
import ir.dotin.platform.pangaea.security.api.ServiceTokenRequest;
import ir.dotin.loan.trade.e2e.security.E2EAuthenticationContextHolder;

@TestConfiguration
public class E2ETestConfiguration {

    private static final String POSTGRES_SERVICE = "postgres";
    private static final int POSTGRES_PORT = 5432;
    private static final String KAFKA_SERVICE = "kafka";
    // 9092 is the PLAINTEXT inter-broker/healthcheck listener; clients use the SASL_PLAINTEXT one.
    private static final int KAFKA_SASL_PORT = 9094;
    private static final String REDIS_SERVICE = "redis";
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_PASSWORD = "e2e_redis_pass";

    private static final ComposeContainer SHARED_CONTAINER = createContainer();

    private static ComposeContainer createContainer() {
        File composeFile = new File("src/test/resources/e2e/docker-compose-e2e.yml");
        // waitingFor, never withExposedService: every service in this compose file runs with
        // `network_mode: host` and binds its fixed port on the host, so there is nothing to publish.
        // withExposedService additionally starts a socat ambassador linked to the service, and Docker
        // rejects that outright — "host type networking can't be used with links". waitingFor applies the
        // same healthcheck wait without the ambassador. The fixed localhost:<port> values below are what
        // the app connects to.
        ComposeContainer container = new ComposeContainer(composeFile)
                .waitingFor(POSTGRES_SERVICE, Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(2)))
                .waitingFor(KAFKA_SERVICE, Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(3)))
                // Redis gets 3 minutes, not 1: its healthcheck waits for cluster_state:ok, which only flips
                // once the redis-bootstrap sidecar has assigned all 16384 slots. That sidecar's own retry
                // budget is ~100s, so a 1-minute wait here can expire before the cluster is legitimately up.
                .waitingFor(REDIS_SERVICE, Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(3)));
        container.start();
        return container;
    }

    @Bean
    ComposeContainer composeContainer() {
        return SHARED_CONTAINER;
    }

    @Bean
    @Primary
    AuthenticationContextHolder e2eAuthenticationContextHolder() {
        return new E2EAuthenticationContextHolder();
    }

    // Same shape as the ServiceTokenProvider stub below: the messaging command authenticator injects a
    // JwtDecoder that only the enabled security starter contributes. Inbound messages in the e2e stack are
    // produced by the tests themselves, so nothing real is ever verified against this.
    @Bean
    @Primary
    JwtDecoder e2eJwtDecoder() {
        Instant now = Instant.now();
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .subject(E2EAuthenticationContextHolder.BRANCH_CODE)
                .claim("sub", "e2e-user")
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofHours(1)))
                .build();
    }

    // The messaging command authenticator pairs the JwtDecoder above with a Jwt -> Authentication converter,
    // also contributed only by the enabled security starter. Spring's stock converter is enough here.
    @Bean
    @Primary
    Converter<Jwt, AbstractAuthenticationToken> e2eJwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }

    // The FCB Kafka request/reply client needs a ServiceTokenProvider to stamp outbound calls, and the real
    // one only exists when pangaea.security is enabled — which would drag in SSO. Every FCB port is mocked
    // here, so no token is ever put on the wire; this only has to satisfy the injection point.
    @Bean
    @Primary
    ServiceTokenProvider e2eServiceTokenProvider() {
        OAuth2TokenResponse token = new OAuth2TokenResponse(
                "e2e-service-token", "Bearer", 3600L, "core", null, Instant.now().getEpochSecond());
        return new ServiceTokenProvider() {

            @Override
            public OAuth2TokenResponse getServiceToken() {
                return token;
            }

            @Override
            public OAuth2TokenResponse getServiceToken(ServiceTokenRequest request) {
                return token;
            }
        };
    }

    @Bean
    @Primary
    // 'container' is injected (not read) only to order this bean after the compose stack has started.
    LettuceConnectionFactory e2eRedisConnectionFactory(ComposeContainer container) {
        // Host networking (broken dev-host DNAT): services run with `network_mode: host` and bind their fixed ports
        // directly on the host, so the Testcontainers ambassador lookup (getServiceHost/getServicePort, which assume
        // published/DNAT ports) does not apply — connect to the fixed localhost:<port> instead.
        RedisClusterConfiguration cfg = new RedisClusterConfiguration(List.of("localhost:" + REDIS_PORT));
        cfg.setPassword(RedisPassword.of(REDIS_PASSWORD));
        LettuceClientConfiguration clientCfg = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(3))
                .shutdownTimeout(Duration.ofMillis(100))
                .build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(cfg, clientCfg);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    // 'container' is injected (not read) only to order this registrar after the compose stack has started.
    DynamicPropertyRegistrar dynamicPropertyRegistrar(ComposeContainer container) {
        return registry -> {
            // Host networking (broken dev-host DNAT): every compose service runs with `network_mode: host` and binds
            // its fixed port on the host, so use fixed localhost:<port> rather than the ambassador
            // getServiceHost/getServicePort lookups (which resolve published/DNAT ports that no longer exist).
            String pgUrl = String.format("jdbc:postgresql://localhost:%d/trade_loan_e2e", POSTGRES_PORT);

            registry.add("spring.datasource.url", () -> pgUrl);
            registry.add("spring.datasource.username", () -> "e2e_user");
            registry.add("spring.datasource.password", () -> "e2e_password");

            // Kafka SASL listener advertises localhost:9094 (see docker-compose-e2e.yml); connect there directly.
            String kafkaBootstrap = "localhost:" + KAFKA_SASL_PORT;
            registry.add("spring.kafka.bootstrap-servers", () -> kafkaBootstrap);
            registry.add("platform.messaging.kafka.bootstrap-servers", () -> kafkaBootstrap);
            registry.add("KAFKA_BOOTSTRAP_SERVERS", () -> kafkaBootstrap);

            registry.add("spring.data.redis.cluster.nodes", () -> "localhost:" + REDIS_PORT);
            registry.add("spring.data.redis.cluster.max-redirects", () -> "5");
            registry.add("spring.data.redis.lettuce.cluster.refresh.period", () -> "5s");
            registry.add("spring.data.redis.lettuce.cluster.refresh.dynamic-refresh-sources", () -> "true");
            registry.add("spring.data.redis.password", () -> REDIS_PASSWORD);
            registry.add("REDIS_PASSWORD", () -> REDIS_PASSWORD);
            registry.add("REDIS_CLUSTER_NODES", () -> "localhost:" + REDIS_PORT);
            registry.add("REDIS_TLS_ENABLED", () -> "false");
        };
    }
}
