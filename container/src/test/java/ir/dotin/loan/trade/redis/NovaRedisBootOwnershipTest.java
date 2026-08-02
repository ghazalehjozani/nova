package ir.dotin.loan.trade.redis;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import ir.dotin.platform.pangaea.servicelayer.starter.autoconfigure.RedisClientAutoConfiguration;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.cluster.ClusterClientOptions;

import static org.assertj.core.api.Assertions.assertThat;

class NovaRedisBootOwnershipTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(DataRedisAutoConfiguration.class, RedisClientAutoConfiguration.class))
            .withPropertyValues(
                    "spring.data.redis.password=secret",
                    "spring.data.redis.client-name=nova-service-redis",
                    "spring.data.redis.timeout=3s",
                    "spring.data.redis.connect-timeout=2s",
                    "spring.data.redis.ssl.enabled=false",
                    "spring.data.redis.cluster.nodes=n1:6379,n2:6379,n3:6379,n4:6379,n5:6379,n6:6379",
                    "spring.data.redis.cluster.max-redirects=5",
                    "spring.data.redis.lettuce.read-from=upstream",
                    "spring.data.redis.lettuce.shutdown-timeout=2s");

    @Test
    void sixSeedsBindToBootOwnedUnpooledFactoryWithSharedOptions() {
        runner.withPropertyValues("spring.data.redis.lettuce.pool.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisConnectionFactory.class);
                    LettuceConnectionFactory factory = context.getBean(LettuceConnectionFactory.class);
                    assertThat(factory.getClusterConfiguration().getClusterNodes())
                            .extracting(RedisNode::asString)
                            .containsExactlyInAnyOrder(
                                    "n1:6379", "n2:6379", "n3:6379", "n4:6379", "n5:6379", "n6:6379");
                    assertThat(factory.getClusterConfiguration().getUsername()).isNull();
                    assertThat(factory.getClientConfiguration().getClass().getSimpleName())
                            .doesNotContain("Pooling");
                    assertThat(factory.getClientConfiguration().getClientName()).contains("nova-service-redis");
                    assertThat(factory.getClientConfiguration().getCommandTimeout())
                            .isEqualTo(Duration.ofSeconds(3));
                    assertThat(factory.getClientConfiguration().getShutdownTimeout())
                            .isEqualTo(Duration.ofSeconds(2));
                    assertThat(factory.getClientConfiguration().getReadFrom()).contains(ReadFrom.UPSTREAM);

                    ClusterClientOptions options = (ClusterClientOptions)
                            factory.getClientConfiguration().getClientOptions().orElseThrow();
                    assertThat(options.getMaxRedirects()).isEqualTo(5);
                    assertThat(options.isAutoReconnect()).isTrue();
                    assertThat(options.getDisconnectedBehavior())
                            .isEqualTo(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS);
                    assertThat(options.getRequestQueueSize()).isEqualTo(2048);
                    assertThat(options.getTopologyRefreshOptions().getRefreshPeriod())
                            .isEqualTo(Duration.ofSeconds(30));
                });
    }

    @Test
    void legacyPoolEnabledConfigStillStartsDuringConfigRollout() {
        runner.withPropertyValues("spring.data.redis.lettuce.pool.enabled=true").run(context -> {
            assertThat(context).hasNotFailed();
            LettuceConnectionFactory factory = context.getBean(LettuceConnectionFactory.class);
            assertThat(factory.getClientConfiguration().getClass().getSimpleName())
                    .contains("Pooling");
        });
    }
}
