# Kafka Consumer Group Storm Fix — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate `TimeoutException: Timeout of 60000ms expired before the position for partition […] could be determined` by replacing wildcard reply subscriptions with explicit per-topic factories, stabilizing consumer group IDs, and tuning staging timeouts so a slow broker no longer trips rebalance storms.

**Architecture:** Introduce a `KafkaListenerContainerFactoryProvider` SPI in `platform-spring-boot-starter-messaging-kafka` that vends per-domain `ConcurrentKafkaListenerContainerFactory` and `ConcurrentMessageListenerContainer` instances bound to **explicit topic lists** (no `Pattern`). Replace `INSTANCE_ID = UUID.randomUUID()` with a deterministic ID derived from `${HOSTNAME}` + `${platform.messaging.kafka.client-id}` so offsets survive restarts. Surface `defaultApiTimeout` / `requestTimeout` / `metadataMaxAge` knobs on consumer + admin configs and override them in the staging profile. Refactor `FcbKafkaConfig` to consume the SPI so `fcbIntegrationRepliesContainer` and `fcbHealthRepliesContainer` are guaranteed independent — `FcbHealthProbe` never blocks on integration-topic rebalance.

**Tech Stack:** Java 25, Spring Boot 4, Spring Kafka 3.x, `ConcurrentKafkaListenerContainerFactory`, `ReplyingKafkaTemplate`, JUnit 5 + Spring Kafka Test (`EmbeddedKafkaBroker`), Lombok, Jackson 3.

---

## Problem evidence (locked from log + code review)

| Symptom | Root cause | Affected code |
|---|---|---|
| `Timeout of 60000ms expired before the position for partition [TOPIC] could be determined` | One consumer subscribes via `Pattern.compile(replyTopicPattern)` matching 5+ topics × ~4 partitions each = 20+ partitions; broker can't return offsets within 60s on slow staging hardware | `KafkaMessagingAutoConfiguration.repliesContainer` lines 166–182 |
| Rebalance storms every restart | `INSTANCE_ID = UUID.randomUUID()` regenerates per JVM, so broker treats every pod-restart as a brand-new group + must rebuild metadata | `KafkaMessagingAutoConfiguration:52` and `FcbKafkaConfig:100,119` |
| `FcbHealthProbe` flaps when integration topic rebalances | Both reply containers share `fcbReplyConsumerFactory`; integration rebalance starves the same broker connection used by the health container | `FcbKafkaConfig:93–130` (separate containers but identical group-naming pattern + factory) |
| No staging-specific timeout override | `default.api.timeout.ms` defaults to 60s; no setting exposed in `MessagingProperties.KafkaConfig.ConsumerConfig` | `MessagingProperties.KafkaConfig.ConsumerConfig` lines 121–141 |

---

## File structure (additions + edits)

```
platform-ddd-framework/
  platform-messaging/platform-spring-boot-starter-messaging-kafka/
    src/main/java/ir/dotin/platform/messaging/kafka/
      spi/
        KafkaListenerContainerFactoryProvider.java         NEW — SPI interface
        TopicBinding.java                                  NEW — record (name, groupSuffix, concurrency)
        DefaultKafkaListenerContainerFactoryProvider.java  NEW — Spring impl
      autoconfigure/
        KafkaMessagingAutoConfiguration.java               MODIFY — drop pattern subscription, expose SPI
        InstanceIdResolver.java                            NEW — derives stable id from env
    src/test/java/ir/dotin/platform/messaging/kafka/
      spi/DefaultKafkaListenerContainerFactoryProviderTest.java   NEW
      autoconfigure/InstanceIdResolverTest.java                   NEW
  platform-messaging/platform-spring-boot-starter-messaging/
    src/main/java/ir/dotin/platform/messaging/autoconfigure/
      MessagingProperties.java                             MODIFY — add ConsumerConfig fields:
                                                            defaultApiTimeout, requestTimeout, metadataMaxAge

trade-loan/
  adapters/driven/fcb-messaging/
    src/main/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/config/
      FcbKafkaConfig.java                                  MODIFY — consume SPI, stable group ids
      FcbKafkaTopicBindings.java                           NEW — central registry of topic bindings
    src/test/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/config/
      FcbKafkaConfigTest.java                              NEW — embedded broker, explicit-topic assertion

nova-config/
  kv/nova-service/messaging.yml                            MODIFY — remove reply-topic-pattern,
                                                            add staging consumer timeouts,
                                                            stable instance-id env var
```

---

## Task 1: Add stable instance-id resolver

**Files:**
- Create: `platform-ddd-framework/platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/autoconfigure/InstanceIdResolver.java`
- Create test: `platform-ddd-framework/platform-messaging/platform-spring-boot-starter-messaging-kafka/src/test/java/ir/dotin/platform/messaging/kafka/autoconfigure/InstanceIdResolverTest.java`

- [ ] **Step 1: Write failing test**

```java
package ir.dotin.platform.messaging.kafka.autoconfigure;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InstanceIdResolverTest {

    @Test
    void resolvesFromExplicitInstanceIdProperty() {
        String id = InstanceIdResolver.resolve("explicit-pod-1", null, "nova-client");
        assertThat(id).isEqualTo("explicit-pod-1");
    }

    @Test
    void resolvesFromHostnameWhenInstanceIdAbsent() {
        String id = InstanceIdResolver.resolve(null, "nova-trade-loan-7d8b", "nova-client");
        assertThat(id).isEqualTo("nova-trade-loan-7d8b");
    }

    @Test
    void fallsBackToClientIdHashWhenHostnameAbsent() {
        String id = InstanceIdResolver.resolve(null, null, "nova-client");
        assertThat(id).isEqualTo("nova-client");
    }

    @Test
    void rejectsBlankClientId() {
        assertThatThrownBy(() -> InstanceIdResolver.resolve(null, null, "  "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot derive stable kafka instance id");
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

Run: `mvn -pl platform-messaging/platform-spring-boot-starter-messaging-kafka -Dtest=InstanceIdResolverTest test`
Expected: FAIL — `InstanceIdResolver` not found.

- [ ] **Step 3: Implement resolver**

```java
package ir.dotin.platform.messaging.kafka.autoconfigure;

public final class InstanceIdResolver {

    private InstanceIdResolver() {}

    public static String resolve(String explicitInstanceId, String hostname, String clientId) {
        if (isNotBlank(explicitInstanceId)) return explicitInstanceId.trim();
        if (isNotBlank(hostname))           return hostname.trim();
        if (isNotBlank(clientId))           return clientId.trim();
        throw new IllegalStateException("cannot derive stable kafka instance id: "
                + "set platform.messaging.kafka.instance-id, HOSTNAME, or platform.messaging.kafka.client-id");
    }

    private static boolean isNotBlank(String s) { return s != null && !s.trim().isEmpty(); }
}
```

- [ ] **Step 4: Run tests, expect pass**

Run: `mvn -pl platform-messaging/platform-spring-boot-starter-messaging-kafka -Dtest=InstanceIdResolverTest test`
Expected: PASS — 4 tests.

- [ ] **Step 5: Commit**

```bash
git add platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/autoconfigure/InstanceIdResolver.java \
        platform-messaging/platform-spring-boot-starter-messaging-kafka/src/test/java/ir/dotin/platform/messaging/kafka/autoconfigure/InstanceIdResolverTest.java
git commit -m "feat(kafka): deterministic instance id resolver for stable consumer groups"
```

---

## Task 2: Extend `MessagingProperties.KafkaConfig.ConsumerConfig` with staging timeouts + instance-id

**Files:**
- Modify: `platform-ddd-framework/platform-messaging/platform-spring-boot-starter-messaging/src/main/java/ir/dotin/platform/messaging/autoconfigure/MessagingProperties.java` (add fields in `KafkaConfig` line 49 region + `ConsumerConfig` lines 121–141)

- [ ] **Step 1: Add `instanceId` field to `KafkaConfig`** (just after `clientId` at line 54)

```java
        /** Stable per-pod identifier appended to consumer group IDs.
         *  Falls back to ${HOSTNAME} then to clientId via InstanceIdResolver. */
        private String instanceId;
```

- [ ] **Step 2: Extend `ConsumerConfig` (lines 121–141)** with three new `Duration` fields

```java
            @NotNull
            private Duration defaultApiTimeout = Duration.ofSeconds(60);

            @NotNull
            private Duration requestTimeout = Duration.ofSeconds(30);

            @NotNull
            private Duration metadataMaxAge = Duration.ofMinutes(5);
```

- [ ] **Step 3: Compile** — `mvn -pl platform-messaging/platform-spring-boot-starter-messaging compile`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add platform-messaging/platform-spring-boot-starter-messaging/src/main/java/ir/dotin/platform/messaging/autoconfigure/MessagingProperties.java
git commit -m "feat(messaging-props): expose kafka consumer timeouts + stable instance-id"
```

---

## Task 3: Define SPI — `KafkaListenerContainerFactoryProvider`

**Files:**
- Create: `platform-ddd-framework/platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/spi/TopicBinding.java`
- Create: `platform-ddd-framework/platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/spi/KafkaListenerContainerFactoryProvider.java`

- [ ] **Step 1: Create `TopicBinding` record**

```java
package ir.dotin.platform.messaging.kafka.spi;

import java.util.List;
import org.springframework.util.Assert;

/** Explicit topic list + group-suffix + concurrency for a single consumer container. */
public record TopicBinding(List<String> topics, String groupSuffix, int concurrency) {

    public TopicBinding {
        Assert.notEmpty(topics, "topics must be non-empty (no wildcards)");
        Assert.hasText(groupSuffix, "groupSuffix must be non-blank");
        Assert.isTrue(concurrency >= 1, "concurrency must be >= 1");
        topics.forEach(t -> Assert.isTrue(
                !t.contains("*") && !t.contains("\\") && !t.contains(".*"),
                "topic must be an explicit name (no regex/wildcards): " + t));
    }

    public static TopicBinding single(String topic, String groupSuffix, int concurrency) {
        return new TopicBinding(List.of(topic), groupSuffix, concurrency);
    }
}
```

- [ ] **Step 2: Create SPI interface**

```java
package ir.dotin.platform.messaging.kafka.spi;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

/**
 * Vends per-topic container factories + containers.
 *
 * <p>Every container is bound to an explicit topic list (no wildcard patterns) and a
 * deterministic groupId of the form
 * {@code <base-group-id>.<groupSuffix>.<instanceId>} so offsets survive restarts.
 */
public interface KafkaListenerContainerFactoryProvider {

    <K, V> ConcurrentKafkaListenerContainerFactory<K, V> factoryFor(
            String domainName,
            ConsumerFactory<K, V> consumerFactory,
            TopicBinding binding);

    <K, V> ConcurrentMessageListenerContainer<K, V> containerFor(
            ConsumerFactory<K, V> consumerFactory,
            TopicBinding binding);

    String groupIdFor(String groupSuffix);
}
```

- [ ] **Step 3: Compile** — `mvn -pl platform-messaging/platform-spring-boot-starter-messaging-kafka compile`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/spi/
git commit -m "feat(kafka-spi): introduce KafkaListenerContainerFactoryProvider + TopicBinding"
```

---

## Task 4: Default SPI implementation + tests

**Files:**
- Create: `platform-ddd-framework/platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/spi/DefaultKafkaListenerContainerFactoryProvider.java`
- Create test: `…/test/java/ir/dotin/platform/messaging/kafka/spi/DefaultKafkaListenerContainerFactoryProviderTest.java`

- [ ] **Step 1: Write failing test**

```java
package ir.dotin.platform.messaging.kafka.spi;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultKafkaListenerContainerFactoryProviderTest {

    private DefaultKafkaConsumerFactory<String, String> stubConsumerFactory() {
        Map<String, Object> c = new HashMap<>();
        c.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        c.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        c.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(c);
    }

    @Test
    void containerSubscribesToExplicitTopicListNotPattern() {
        var provider = new DefaultKafkaListenerContainerFactoryProvider(
                "core.loan.nova.fcb-integration.v1", "pod-7d8b");
        var binding = TopicBinding.single("corridor.core.loan.nova.x.response.queue.v1", "x-reply", 2);

        ConcurrentMessageListenerContainer<String, String> c =
                provider.containerFor(stubConsumerFactory(), binding);

        assertThat(c.getContainerProperties().getTopics())
                .containsExactly("corridor.core.loan.nova.x.response.queue.v1");
        assertThat(c.getContainerProperties().getTopicPattern()).isNull();
        assertThat(c.getContainerProperties().getGroupId())
                .isEqualTo("core.loan.nova.fcb-integration.v1.x-reply.pod-7d8b");
        assertThat(c.getConcurrency()).isEqualTo(2);
    }

    @Test
    void rejectsWildcardTopic() {
        assertThatThrownBy(() -> TopicBinding.single("foo.*", "x", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("explicit name");
    }

    @Test
    void groupIdFormatIsStableAcrossInvocations() {
        var p = new DefaultKafkaListenerContainerFactoryProvider("base", "host-1");
        assertThat(p.groupIdFor("reply")).isEqualTo("base.reply.host-1");
        assertThat(p.groupIdFor("reply")).isEqualTo("base.reply.host-1");
    }
}
```

- [ ] **Step 2: Run, expect fail**

Run: `mvn -pl platform-messaging/platform-spring-boot-starter-messaging-kafka -Dtest=DefaultKafkaListenerContainerFactoryProviderTest test`
Expected: FAIL — class not found.

- [ ] **Step 3: Implement**

```java
package ir.dotin.platform.messaging.kafka.spi;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.util.Assert;

public class DefaultKafkaListenerContainerFactoryProvider implements KafkaListenerContainerFactoryProvider {

    private final String baseGroupId;
    private final String instanceId;

    public DefaultKafkaListenerContainerFactoryProvider(String baseGroupId, String instanceId) {
        Assert.hasText(baseGroupId, "baseGroupId required");
        Assert.hasText(instanceId, "instanceId required");
        this.baseGroupId = baseGroupId;
        this.instanceId = instanceId;
    }

    @Override
    public String groupIdFor(String groupSuffix) {
        return baseGroupId + "." + groupSuffix + "." + instanceId;
    }

    @Override
    public <K, V> ConcurrentKafkaListenerContainerFactory<K, V> factoryFor(
            String domainName, ConsumerFactory<K, V> consumerFactory, TopicBinding binding) {
        var factory = new ConcurrentKafkaListenerContainerFactory<K, V>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(binding.concurrency());
        factory.setBeanName("kafkaListenerContainerFactory." + domainName);
        factory.getContainerProperties().setGroupId(groupIdFor(binding.groupSuffix()));
        return factory;
    }

    @Override
    public <K, V> ConcurrentMessageListenerContainer<K, V> containerFor(
            ConsumerFactory<K, V> consumerFactory, TopicBinding binding) {
        ContainerProperties props = new ContainerProperties(binding.topics().toArray(String[]::new));
        props.setGroupId(groupIdFor(binding.groupSuffix()));
        var container = new ConcurrentMessageListenerContainer<>(consumerFactory, props);
        container.setConcurrency(binding.concurrency());
        container.setAutoStartup(true);
        return container;
    }
}
```

- [ ] **Step 4: Run tests, expect pass**

Run: `mvn -pl platform-messaging/platform-spring-boot-starter-messaging-kafka -Dtest=DefaultKafkaListenerContainerFactoryProviderTest test`
Expected: PASS — 3 tests.

- [ ] **Step 5: Commit**

```bash
git add platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/spi/DefaultKafkaListenerContainerFactoryProvider.java \
        platform-messaging/platform-spring-boot-starter-messaging-kafka/src/test/java/ir/dotin/platform/messaging/kafka/spi/DefaultKafkaListenerContainerFactoryProviderTest.java
git commit -m "feat(kafka-spi): DefaultKafkaListenerContainerFactoryProvider"
```

---

## Task 5: Wire SPI into `KafkaMessagingAutoConfiguration` — drop `INSTANCE_ID` UUID, drop pattern container

**Files:**
- Modify: `platform-ddd-framework/platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/autoconfigure/KafkaMessagingAutoConfiguration.java`

- [ ] **Step 1: Replace `public static final String INSTANCE_ID = UUID.randomUUID().toString();` (line 52) with a bean-resolved static accessor**

```java
    // Set by the auto-configuration once at startup so legacy callers keep working
    public static volatile String INSTANCE_ID;
```

- [ ] **Step 2: Add `@Bean` `instanceId` + `kafkaListenerContainerFactoryProvider`** right after the constructor (insert at line 67):

```java
    @Bean
    @ConditionalOnMissingBean(name = "kafkaMessagingInstanceId")
    public String kafkaMessagingInstanceId(
            @Value("${HOSTNAME:}") String hostname) {
        String id = InstanceIdResolver.resolve(
                properties.getKafka().getInstanceId(),
                hostname,
                properties.getKafka().getClientId());
        INSTANCE_ID = id; // legacy callers
        return id;
    }

    @Bean
    @ConditionalOnMissingBean(KafkaListenerContainerFactoryProvider.class)
    public KafkaListenerContainerFactoryProvider kafkaListenerContainerFactoryProvider(
            String kafkaMessagingInstanceId) {
        return new DefaultKafkaListenerContainerFactoryProvider(
                properties.getKafka().getConsumerGroupId(),
                kafkaMessagingInstanceId);
    }
```

- [ ] **Step 3: Delete the wildcard `repliesContainer` bean (lines 163–182) entirely** — replaced by per-domain bindings in consuming modules. Also delete the now-orphaned `replyingKafkaTemplate` bean (lines 184–195). Add an inline comment at the deletion site:

```java
    // Pattern-based reply container removed (CBS-XXX). Consuming modules must
    // declare explicit per-topic containers via KafkaListenerContainerFactoryProvider.
```

- [ ] **Step 4: Add staging timeouts to `createConsumerConfigs()` (line 203)** — append before `addSecurityConfigs`:

```java
        configs.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, (int)
                kafkaConfig.getConsumer().getDefaultApiTimeout().toMillis());
        configs.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, (int)
                kafkaConfig.getConsumer().getRequestTimeout().toMillis());
        configs.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, (int)
                kafkaConfig.getConsumer().getMetadataMaxAge().toMillis());
```

- [ ] **Step 5: Imports** — remove `import java.util.UUID;`, `import java.util.regex.Pattern;`, `import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;`, `import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;` (now unused here). Add `import ir.dotin.platform.messaging.kafka.spi.DefaultKafkaListenerContainerFactoryProvider;`, `import ir.dotin.platform.messaging.kafka.spi.KafkaListenerContainerFactoryProvider;`.

- [ ] **Step 6: Compile + run existing tests**

Run: `mvn -pl platform-messaging/platform-spring-boot-starter-messaging-kafka test`
Expected: PASS — no compile errors, no pattern-container references.

- [ ] **Step 7: Commit**

```bash
git add platform-messaging/platform-spring-boot-starter-messaging-kafka/src/main/java/ir/dotin/platform/messaging/kafka/autoconfigure/KafkaMessagingAutoConfiguration.java
git commit -m "refactor(kafka): drop wildcard reply container + stable instance id"
```

---

## Task 6: `FcbKafkaTopicBindings` central registry

**Files:**
- Create: `trade-loan/adapters/driven/fcb-messaging/src/main/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/config/FcbKafkaTopicBindings.java`

- [ ] **Step 1: Implement registry**

```java
package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import ir.dotin.platform.messaging.kafka.spi.TopicBinding;

/** Single source of truth for FCB Kafka topic→groupSuffix→concurrency wiring.
 *  Each binding produces an isolated consumer group; no topic appears in more than one binding. */
public final class FcbKafkaTopicBindings {

    private FcbKafkaTopicBindings() {}

    public static TopicBinding integrationReply(FcbKafkaProperties props, int concurrency) {
        return TopicBinding.single(props.getReplyTopic(), "fcb-integration-reply", concurrency);
    }

    public static TopicBinding healthReply(FcbKafkaProperties props) {
        // health probe MUST be isolated — concurrency=1, dedicated group
        return TopicBinding.single(props.getHealthReplyTopic(), "fcb-health-reply", 1);
    }
}
```

- [ ] **Step 2: Compile** — `./gradlew :adapters-driven-fcb-messaging:compileJava`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add adapters/driven/fcb-messaging/src/main/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/config/FcbKafkaTopicBindings.java
git commit -m "feat(fcb-kafka): topic bindings registry"
```

---

## Task 7: Refactor `FcbKafkaConfig` to consume the SPI

**Files:**
- Modify: `trade-loan/adapters/driven/fcb-messaging/src/main/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/config/FcbKafkaConfig.java`

- [ ] **Step 1: Replace `fcbIntegrationRepliesContainer` (lines 93–111)**

```java
    @Bean(FCB_INTEGRATION_REPLIES_CONTAINER)
    public ConcurrentMessageListenerContainer<String, byte[]> fcbIntegrationRepliesContainer(
            @Qualifier(FCB_REPLY_CONSUMER_FACTORY) ConsumerFactory<String, byte[]> fcbReplyConsumerFactory,
            FcbKafkaProperties properties,
            KafkaListenerContainerFactoryProvider provider) {

        ConcurrentMessageListenerContainer<String, byte[]> container =
                provider.containerFor(
                        fcbReplyConsumerFactory,
                        FcbKafkaTopicBindings.integrationReply(properties, Math.max(1, CORES)));
        container.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        return container;
    }
```

- [ ] **Step 2: Replace `fcbHealthRepliesContainer` (lines 113–130)**

```java
    @Bean(FCB_HEALTH_REPLIES_CONTAINER)
    public ConcurrentMessageListenerContainer<String, byte[]> fcbHealthRepliesContainer(
            @Qualifier(FCB_REPLY_CONSUMER_FACTORY) ConsumerFactory<String, byte[]> fcbReplyConsumerFactory,
            FcbKafkaProperties properties,
            KafkaListenerContainerFactoryProvider provider) {

        ConcurrentMessageListenerContainer<String, byte[]> container =
                provider.containerFor(
                        fcbReplyConsumerFactory,
                        FcbKafkaTopicBindings.healthReply(properties));
        container.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        return container;
    }
```

- [ ] **Step 3: Remove stale imports** — `import ir.dotin.platform.messaging.kafka.autoconfigure.KafkaMessagingAutoConfiguration;` and `import org.springframework.beans.factory.annotation.Value;` (no longer needed — provider handles group-id).
Add: `import ir.dotin.platform.messaging.kafka.spi.KafkaListenerContainerFactoryProvider;`.

- [ ] **Step 4: Compile** — `./gradlew :adapters-driven-fcb-messaging:compileJava`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add adapters/driven/fcb-messaging/src/main/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/config/FcbKafkaConfig.java
git commit -m "refactor(fcb-kafka): consume KafkaListenerContainerFactoryProvider SPI"
```

---

## Task 8: Integration test — embedded broker proves explicit subscription + stable group id

**Files:**
- Create: `trade-loan/adapters/driven/fcb-messaging/src/test/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/config/FcbKafkaConfigTest.java`

- [ ] **Step 1: Write failing test**

```java
package ir.dotin.loan.trade.adapters.driven.fcbmessaging.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"kafka-fcb"})
@EmbeddedKafka(partitions = 4, topics = {
        "corridor.core.loan.nova.fcb-integration.response.queue.v1",
        "corridor.core.loan.nova.fcb-health-check.response.queue.v1"
})
@TestPropertySource(properties = {
        "platform.messaging.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "platform.messaging.kafka.consumer-group-id=test.fcb.v1",
        "platform.messaging.kafka.client-id=test-client",
        "platform.messaging.kafka.instance-id=test-pod-1",
        "nova.fcb.kafka.request-topic=corridor.core.loan.nova.fcb-integration.request.queue.v1",
        "nova.fcb.kafka.reply-topic=corridor.core.loan.nova.fcb-integration.response.queue.v1",
        "nova.fcb.kafka.health-request-topic=corridor.core.loan.nova.fcb-health-check.request.queue.v1",
        "nova.fcb.kafka.health-reply-topic=corridor.core.loan.nova.fcb-health-check.response.queue.v1"
})
class FcbKafkaConfigTest {

    @Autowired
    @Qualifier(FcbKafkaConfig.FCB_INTEGRATION_REPLIES_CONTAINER)
    ConcurrentMessageListenerContainer<String, byte[]> integrationContainer;

    @Autowired
    @Qualifier(FcbKafkaConfig.FCB_HEALTH_REPLIES_CONTAINER)
    ConcurrentMessageListenerContainer<String, byte[]> healthContainer;

    @Test
    void integrationContainerSubscribesOnlyToIntegrationReplyTopic() {
        assertThat(integrationContainer.getContainerProperties().getTopics())
                .containsExactly("corridor.core.loan.nova.fcb-integration.response.queue.v1");
        assertThat(integrationContainer.getContainerProperties().getTopicPattern()).isNull();
    }

    @Test
    void healthContainerSubscribesOnlyToHealthReplyTopic() {
        assertThat(healthContainer.getContainerProperties().getTopics())
                .containsExactly("corridor.core.loan.nova.fcb-health-check.response.queue.v1");
        assertThat(healthContainer.getContainerProperties().getTopicPattern()).isNull();
    }

    @Test
    void groupIdsAreStableAndDistinct() {
        String integrationGroup = integrationContainer.getContainerProperties().getGroupId();
        String healthGroup = healthContainer.getContainerProperties().getGroupId();
        assertThat(integrationGroup).isEqualTo("test.fcb.v1.fcb-integration-reply.test-pod-1");
        assertThat(healthGroup).isEqualTo("test.fcb.v1.fcb-health-reply.test-pod-1");
        assertThat(integrationGroup).isNotEqualTo(healthGroup);
    }

    @Test
    void healthContainerConcurrencyIsOne() {
        assertThat(healthContainer.getConcurrency()).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Run, expect pass** (after Tasks 5–7 merged)

Run: `./gradlew :adapters-driven-fcb-messaging:test --tests FcbKafkaConfigTest`
Expected: PASS — 4 tests, embedded broker boots, both containers report explicit topics and distinct stable group IDs.

- [ ] **Step 3: Commit**

```bash
git add adapters/driven/fcb-messaging/src/test/java/ir/dotin/loan/trade/adapters/driven/fcbmessaging/config/FcbKafkaConfigTest.java
git commit -m "test(fcb-kafka): assert explicit subscriptions + stable group ids"
```

---

## Task 9: Update `nova-config` — remove wildcard pattern, add staging consumer timeouts + instance-id

**Files:**
- Modify: `nova-config/kv/nova-service/messaging.yml`

- [ ] **Step 1: Delete the `reply` block at lines 93–95** (wildcard pattern is now forbidden):

Find:

```yaml
      reply:
        reply-topic-pattern: "${KAFKA_REPLY_TOPIC_PATTERN:corridor\\.core\\.loan\\.nova\\..*\\.response\\.queue\\.v\\d+$}"
        default-timeout: "${KAFKA_DEFAULT_TIMEOUT:30s}"
```

Replace with:

```yaml
      reply:
        # reply-topic-pattern intentionally removed (CBS-XXX). Per-domain containers
        # declare explicit topic lists via KafkaListenerContainerFactoryProvider.
        default-timeout: "${KAFKA_DEFAULT_TIMEOUT:30s}"
```

- [ ] **Step 2: Add `instance-id` under `platform.messaging.kafka` (insert after `client-id` line 73)**

```yaml
      instance-id: "${KUBERNETES_POD_NAME:${HOSTNAME:}}"
```

- [ ] **Step 3: Add staging consumer timeouts** (extend `consumer` block at lines 86–92):

```yaml
      consumer:
        auto-offset-reset: latest
        enable-auto-commit: false
        max-poll-records: 500
        max-poll-interval: 5m
        session-timeout: 30s
        heartbeat-interval: 10s
        default-api-timeout: "${KAFKA_DEFAULT_API_TIMEOUT:180s}"
        request-timeout: "${KAFKA_REQUEST_TIMEOUT:60s}"
        metadata-max-age: "${KAFKA_METADATA_MAX_AGE:5m}"
```

- [ ] **Step 4: Yaml-lint**

Run: `yamllint kv/nova-service/messaging.yml`
Expected: 0 errors.

- [ ] **Step 5: Commit (nova-config repo)**

```bash
git add kv/nova-service/messaging.yml
git commit -m "chore(nova-config): drop wildcard reply pattern, add staging consumer timeouts + instance-id"
```

---

## Task 10: Staging-profile timeout override

**Files:**
- Modify: `nova-config/kv/nova-service/messaging,staging.yml` (create if missing — follow the `application,elk` overlay convention already present in the repo)

- [ ] **Step 1: Check if a staging overlay exists**

Run: `ls /home/m.amirabdollahi/workspaces/nova-config/kv/nova-service/ | grep -i staging`
Expected: either lists `messaging,staging.yml` or is empty.

- [ ] **Step 2: Create overlay** with bumped timeouts for the slow staging broker:

```yaml
---
platform:
  messaging:
    kafka:
      consumer:
        default-api-timeout: 240s
        request-timeout: 120s
        session-timeout: 60s
        heartbeat-interval: 20s
        metadata-max-age: 10m
```

- [ ] **Step 3: Commit**

```bash
git add kv/nova-service/messaging,staging.yml
git commit -m "chore(nova-config): staging overlay — extend kafka consumer timeouts"
```

---

## Task 11: Smoke-test on staging (manual, no CI gate)

- [ ] **Step 1: Deploy** the three feature branches to staging:
  - `platform-ddd-framework` → release version pinned in `trade-loan/gradle.properties` (bump if a new tag is required).
  - `trade-loan` → roll out one replica first; watch logs for `KafkaMessagingAutoConfiguration` startup + `DefaultKafkaListenerContainerFactoryProvider` group-id line.
  - `nova-config` → reload Consul KV.
- [ ] **Step 2: Verify in staging logs**
  - Search: `groupId=test.fcb.v1.fcb-integration-reply.<pod-hostname>` (or production equivalent) — must contain pod hostname, not a UUID.
  - Search: `Subscribed to topic\(s\):` — each container must list exactly one topic, no `Pattern`.
  - Search for the original error: `Timeout of 60000ms expired before the position for partition` — must NOT recur in the 30 min following rollout.
- [ ] **Step 3: Restart one pod**, confirm group rejoin completes in < 10s (no full rebalance — same group id, same hostname).
- [ ] **Step 4: Hit `FcbHealthProbe` during a forced integration-topic rebalance** (`kafka-consumer-groups.sh --describe --group test.fcb.v1.fcb-integration-reply.<pod>`); health probe latency in metrics dashboard MUST stay below 500 ms.
- [ ] **Step 5: Tag release** if all clean: `git tag -a v2026.5.1 -m "kafka: explicit-topic isolation + stable group ids"`.

---

## Self-Review checklist (run against this plan)

| Item | Status |
|---|---|
| Every task names exact file paths | yes |
| Every code step shows complete code, not "similar to" | yes |
| Wildcard `replyTopicPattern` removed from config AND code | yes (Tasks 5, 9) |
| `INSTANCE_ID = UUID.randomUUID()` eliminated, replaced by `InstanceIdResolver` | yes (Tasks 1, 5) |
| Per-topic explicit subscriptions enforced (`TopicBinding` constructor rejects regex) | yes (Task 3) |
| `FcbHealthProbe` isolation provable by test | yes (Task 8 — distinct groups, concurrency=1) |
| Staging timeouts surfaced + overridden | yes (Tasks 2, 9, 10) |
| SPI is bean-replaceable (`@ConditionalOnMissingBean`) for future centralized infra | yes (Task 5) |
| Test for each behavior change | Tasks 1, 4, 8 cover resolver, SPI, end-to-end |
| No "TODO" / "fill in later" left | confirmed |

---

## Execution Handoff

**Plan complete and saved to `trade-loan/docs/superpowers/plans/2026-05-14-kafka-consumer-group-storm-fix.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — dispatch fresh subagent per task, review between tasks, fast iteration.

**2. Inline Execution** — execute tasks in this session via `superpowers:executing-plans`, batch with checkpoints.

**Which approach?**
