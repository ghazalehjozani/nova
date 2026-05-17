package ir.dotin.loan.trade.e2e;

import java.io.File;
import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration
public class E2ETestConfiguration {

    private static final String POSTGRES_SERVICE = "postgres";
    private static final int POSTGRES_PORT = 5432;
    private static final String KAFKA_SERVICE = "kafka";
    private static final int KAFKA_INTERNAL_PORT = 9092;
    private static final int KAFKA_SASL_PORT = 9094;
    private static final String REDIS_SERVICE = "redis";
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_PASSWORD = "e2e_redis_pass";

    private static final ComposeContainer SHARED_CONTAINER = createContainer();

    private static ComposeContainer createContainer() {
        File composeFile = new File("src/test/resources/e2e/docker-compose-e2e.yml");
        ComposeContainer container = new ComposeContainer(composeFile)
                .withExposedService(
                        POSTGRES_SERVICE,
                        POSTGRES_PORT,
                        Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(2)))
                .withExposedService(
                        KAFKA_SERVICE,
                        KAFKA_INTERNAL_PORT,
                        Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(3)))
                .withExposedService(
                        REDIS_SERVICE, REDIS_PORT, Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(1)));
        container.start();
        return container;
    }

    @Bean
    ComposeContainer composeContainer() {
        return SHARED_CONTAINER;
    }

    @Bean
    @Primary
    LettuceConnectionFactory e2eRedisConnectionFactory(ComposeContainer container) {
        String host = container.getServiceHost(REDIS_SERVICE, REDIS_PORT);
        int port = container.getServicePort(REDIS_SERVICE, REDIS_PORT);
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration(host, port);
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
    DynamicPropertyRegistrar dynamicPropertyRegistrar(ComposeContainer container) {
        return registry -> {
            String pgHost = container.getServiceHost(POSTGRES_SERVICE, POSTGRES_PORT);
            int pgPort = container.getServicePort(POSTGRES_SERVICE, POSTGRES_PORT);
            String pgUrl = String.format("jdbc:postgresql://%s:%d/trade_loan_e2e", pgHost, pgPort);

            registry.add("spring.datasource.url", () -> pgUrl);
            registry.add("spring.datasource.username", () -> "e2e_user");
            registry.add("spring.datasource.password", () -> "e2e_password");

            String kafkaBootstrap = "host.docker.internal:" + KAFKA_SASL_PORT;
            registry.add("spring.kafka.bootstrap-servers", () -> kafkaBootstrap);
            registry.add("platform.messaging.kafka.bootstrap-servers", () -> kafkaBootstrap);
            registry.add("KAFKA_BOOTSTRAP_SERVERS", () -> kafkaBootstrap);

            registry.add("spring.data.redis.sentinel.enabled", () -> "false");
            registry.add("spring.data.redis.password", () -> REDIS_PASSWORD);
            registry.add("REDIS_PASSWORD", () -> REDIS_PASSWORD);
            registry.add("REDIS_MASTER_NAME", () -> "nova-master");
            registry.add("REDIS_SENTINEL_NODES", () -> "127.0.0.1:26379");
            registry.add("REDIS_TLS_ENABLED", () -> "false");
        };
    }
}
