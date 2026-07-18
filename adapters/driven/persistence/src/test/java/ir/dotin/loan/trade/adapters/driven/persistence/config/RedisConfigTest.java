package ir.dotin.loan.trade.adapters.driven.persistence.config;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    void shouldBuildClusterConnectionFactoryFromRedisProperties() {
        DataRedisProperties properties = redisProperties();

        var connectionFactory = redisConfig.redisConnectionFactory(properties, mock(ClientResources.class));

        var clusterConfiguration = connectionFactory.getClusterConfiguration();
        assertThat(clusterConfiguration).isNotNull();
        assertThat(clusterConfiguration.getClusterNodes())
                .extracting(RedisNode::asString)
                .containsExactlyInAnyOrder("redis-1:6379", "redis-2:6379", "redis-3:6379");
        assertThat(clusterConfiguration.getUsername()).isEqualTo("nova");
        assertThat(clusterConfiguration.getPassword()).isEqualTo(RedisPassword.of("secret"));
        assertThat(clusterConfiguration.getMaxRedirects()).isEqualTo(8);

        assertThat(connectionFactory.getClientConfiguration())
                .isInstanceOfSatisfying(LettucePoolingClientConfiguration.class, clientConfiguration -> {
                    assertThat(clientConfiguration.getCommandTimeout()).isEqualTo(Duration.ofSeconds(3));
                    assertThat(clientConfiguration.getShutdownTimeout()).isEqualTo(Duration.ofMillis(200));
                    assertThat(clientConfiguration.getReadFrom()).contains(ReadFrom.REPLICA_PREFERRED);
                    assertThat(clientConfiguration.isUseSsl()).isTrue();

                    var pool = clientConfiguration.getPoolConfig();
                    assertThat(pool.getMaxTotal()).isEqualTo(32);
                    assertThat(pool.getMaxIdle()).isEqualTo(16);
                    assertThat(pool.getMinIdle()).isEqualTo(4);
                    assertThat(pool.getMaxWaitDuration()).isEqualTo(Duration.ofSeconds(2));
                    assertThat(pool.getTestOnBorrow()).isTrue();
                    assertThat(pool.getTestWhileIdle()).isTrue();
                });
    }

    @Test
    void shouldConfigureAdaptiveAndPeriodicClusterTopologyRefresh() {
        DataRedisProperties properties = redisProperties();

        var connectionFactory = redisConfig.redisConnectionFactory(properties, mock(ClientResources.class));

        ClusterClientOptions clientOptions = (ClusterClientOptions)
                connectionFactory.getClientConfiguration().getClientOptions().orElseThrow();
        ClusterTopologyRefreshOptions refreshOptions = clientOptions.getTopologyRefreshOptions();

        assertThat(refreshOptions.getAdaptiveRefreshTriggers())
                .containsExactlyInAnyOrderElementsOf(ClusterTopologyRefreshOptions.DEFAULT_ADAPTIVE_REFRESH_TRIGGERS);
        assertThat(refreshOptions.isPeriodicRefreshEnabled()).isTrue();
        assertThat(refreshOptions.getRefreshPeriod()).isEqualTo(Duration.ofSeconds(30));
        assertThat(refreshOptions.useDynamicRefreshSources()).isTrue();
        assertThat(clientOptions.getMaxRedirects()).isEqualTo(8);
        assertThat(clientOptions.getSocketOptions().getConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
    }

    private DataRedisProperties redisProperties() {
        DataRedisProperties properties = new DataRedisProperties();
        properties.setUsername("nova");
        properties.setPassword("secret");
        properties.setTimeout(Duration.ofSeconds(3));
        properties.setConnectTimeout(Duration.ofSeconds(2));
        properties.getSsl().setEnabled(true);

        DataRedisProperties.Cluster cluster = new DataRedisProperties.Cluster();
        cluster.setNodes(List.of("redis-1:6379", "redis-2:6379", "redis-3:6379"));
        cluster.setMaxRedirects(8);
        properties.setCluster(cluster);

        properties.getLettuce().setShutdownTimeout(Duration.ofMillis(200));
        properties.getLettuce().getCluster().getRefresh().setPeriod(Duration.ofSeconds(30));
        properties.getLettuce().getCluster().getRefresh().setDynamicRefreshSources(true);

        DataRedisProperties.Pool pool = properties.getLettuce().getPool();
        pool.setEnabled(true);
        pool.setMaxActive(32);
        pool.setMaxIdle(16);
        pool.setMinIdle(4);
        pool.setMaxWait(Duration.ofSeconds(2));
        return properties;
    }
}
