package ir.dotin.loan.trade.adapters.driven.persistence.config;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import ir.dotin.platform.pangaea.commons.core.cache.CacheKeyspace;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    @Bean(destroyMethod = "shutdown")
    public ClientResources lettuceClientResources(ObservationRegistry observationRegistry) {
        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        // Lettuce emits Redis CLIENT spans through Micrometer Observation → the OTel bridge turns them into spans
        // (db.system=redis), restoring the Redis dependency node in APM. The Spring Boot Lettuce observation
        // auto-config backs off because this is a custom ClientResources/ConnectionFactory, so wire it explicitly.
        MicrometerTracing tracing = new MicrometerTracing(observationRegistry, "redis", false);
        return DefaultClientResources.builder()
                .ioThreadPoolSize(threads)
                .computationThreadPoolSize(threads)
                .tracing(tracing)
                .build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            DataRedisProperties redisProperties, ClientResources clientResources) {

        DataRedisProperties.Cluster clusterProperties = requiredClusterProperties(redisProperties);
        RedisClusterConfiguration clusterConfig = redisClusterConfiguration(redisProperties, clusterProperties);

        Duration commandTimeout =
                redisProperties.getTimeout() != null ? redisProperties.getTimeout() : Duration.ofSeconds(3);
        Duration connectTimeout = redisProperties.getConnectTimeout() != null
                ? redisProperties.getConnectTimeout()
                : Duration.ofSeconds(2);

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(connectTimeout)
                .keepAlive(true)
                .tcpNoDelay(true)
                .build();

        ClusterClientOptions.Builder clientOptionsBuilder = ClusterClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP3)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .autoReconnect(true)
                .socketOptions(socketOptions)
                .topologyRefreshOptions(clusterTopologyRefreshOptions(redisProperties))
                .timeoutOptions(TimeoutOptions.enabled(commandTimeout));

        Integer maxRedirects = clusterProperties.getMaxRedirects();
        if (maxRedirects != null) {
            clientOptionsBuilder.maxRedirects(maxRedirects);
        }

        boolean tlsEnabled = redisProperties.getSsl().isEnabled();
        if (tlsEnabled) {
            clientOptionsBuilder.sslOptions(
                    SslOptions.builder().jdkSslProvider().build());
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientCfg = lettuceClientConfigurationBuilder(
                        redisProperties)
                .clientResources(clientResources)
                .commandTimeout(commandTimeout)
                .shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout())
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .clientOptions(clientOptionsBuilder.build());

        String clientName = redisProperties.getClientName();
        if (clientName != null && !clientName.isBlank()) {
            clientCfg.clientName(clientName);
        }

        if (tlsEnabled) {
            clientCfg.useSsl();
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(clusterConfig, clientCfg.build());
        factory.setShareNativeConnection(true);
        factory.setValidateConnection(false);
        return factory;
    }

    private RedisClusterConfiguration redisClusterConfiguration(
            DataRedisProperties redisProperties, DataRedisProperties.Cluster clusterProperties) {
        List<String> clusterNodes =
                requireNonNull(clusterProperties.getNodes(), "spring.data.redis.cluster.nodes must be configured");
        if (clusterNodes.isEmpty() || clusterNodes.stream().anyMatch(String::isBlank)) {
            throw new IllegalStateException("spring.data.redis.cluster.nodes must contain valid host:port entries");
        }

        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(clusterNodes);

        if (clusterProperties.getMaxRedirects() != null) {
            clusterConfig.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        String username = redisProperties.getUsername();
        if (username != null && !username.isBlank()) {
            clusterConfig.setUsername(username);
        }
        String password = redisProperties.getPassword();
        if (password != null && !password.isBlank()) {
            clusterConfig.setPassword(RedisPassword.of(password));
        }
        return clusterConfig;
    }

    private ClusterTopologyRefreshOptions clusterTopologyRefreshOptions(DataRedisProperties redisProperties) {
        DataRedisProperties.Lettuce.Cluster.Refresh refreshProperties =
                redisProperties.getLettuce().getCluster().getRefresh();
        ClusterTopologyRefreshOptions.Builder refreshOptions = ClusterTopologyRefreshOptions.builder()
                .dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());

        if (refreshProperties.getPeriod() != null) {
            refreshOptions.enablePeriodicRefresh(refreshProperties.getPeriod());
        }
        return refreshOptions.build();
    }

    private LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigurationBuilder(
            DataRedisProperties redisProperties) {
        DataRedisProperties.Pool poolProperties = redisProperties.getLettuce().getPool();
        if (!Boolean.FALSE.equals(poolProperties.getEnabled())) {
            return LettucePoolingClientConfiguration.builder().poolConfig(redisPoolConfiguration(poolProperties));
        }
        return LettuceClientConfiguration.builder();
    }

    private GenericObjectPoolConfig<StatefulConnection<?, ?>> redisPoolConfiguration(
            DataRedisProperties.Pool poolProperties) {
        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(poolProperties.getMaxActive());
        poolConfig.setMaxIdle(poolProperties.getMaxIdle());
        poolConfig.setMinIdle(poolProperties.getMinIdle());
        poolConfig.setMaxWait(poolProperties.getMaxWait());
        if (poolProperties.getTimeBetweenEvictionRuns() != null) {
            poolConfig.setTimeBetweenEvictionRuns(poolProperties.getTimeBetweenEvictionRuns());
        }
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        return poolConfig;
    }

    private DataRedisProperties.Cluster requiredClusterProperties(DataRedisProperties redisProperties) {
        return requireNonNull(redisProperties.getCluster(), "spring.data.redis.cluster.nodes must be configured");
    }

    @Bean
    public RedisCacheManager redisLoanCacheManager(
            RedisConnectionFactory connectionFactory, @Value("${nova.cache.version}") String cacheKeyVersion) {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        JsonMapper mapper = JsonMapper.builder()
                .activateDefaultTyping(ptv, DefaultTyping.JAVA_LANG_OBJECT, JsonTypeInfo.As.PROPERTY)
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .changeDefaultVisibility(vc -> vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                        .withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY))
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();

        GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(mapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith(CacheKeyspace.versioned("", cacheKeyVersion))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new ResilientCacheErrorHandler();
    }
}
