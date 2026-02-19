package ir.dotin.loan.trade.e2e;

import java.io.File;
import java.time.Duration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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

    // Static singleton: shared across all Spring contexts in the same JVM,
    // preventing multiple Docker Compose startups when REST and messaging tests
    // use different context configurations.
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
    DynamicPropertyRegistrar dynamicPropertyRegistrar(ComposeContainer container) {
        return registry -> {
            String pgHost = container.getServiceHost(POSTGRES_SERVICE, POSTGRES_PORT);
            int pgPort = container.getServicePort(POSTGRES_SERVICE, POSTGRES_PORT);
            String pgUrl = String.format("jdbc:postgresql://%s:%d/trade_loan_e2e", pgHost, pgPort);

            registry.add("spring.datasource.url", () -> pgUrl);
            registry.add("spring.datasource.username", () -> "e2e_user");
            registry.add("spring.datasource.password", () -> "e2e_password");

            // Kafka uses fixed port mapping (9094:9094) with SASL_PLAINTEXT.
            // Advertised listener is host.docker.internal:9094 so clients can resolve broker metadata.
            // This works from devcontainers where localhost doesn't reach Docker host ports.
            String kafkaBootstrap = "host.docker.internal:" + KAFKA_SASL_PORT;

            registry.add("spring.kafka.bootstrap-servers", () -> kafkaBootstrap);
            registry.add("platform.messaging.kafka.bootstrap-servers", () -> kafkaBootstrap);
            registry.add("KAFKA_BOOTSTRAP_SERVERS", () -> kafkaBootstrap);

            String redisHost = container.getServiceHost(REDIS_SERVICE, REDIS_PORT);
            int redisPort = container.getServicePort(REDIS_SERVICE, REDIS_PORT);

            registry.add("spring.data.redis.host", () -> redisHost);
            registry.add("spring.data.redis.port", () -> String.valueOf(redisPort));
            registry.add("spring.data.redis.password", () -> "e2e_redis_pass");
            registry.add("REDIS_HOST", () -> redisHost);
            registry.add("REDIS_PASSWORD", () -> "e2e_redis_pass");
        };
    }
}
